package kalina.compiler.syntax.parser;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.BasicBlock;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.FunBasicBlock;
import kalina.compiler.bb.TypeAndName;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.FunCallExpression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.ObjectCreationExpression;
import kalina.compiler.expressions.RHS;
import kalina.compiler.expressions.ReturnValueInfo;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.VariableInfo;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import kalina.compiler.expressions.operations.ComparisonOperation;
import kalina.compiler.instructions.AssignInstruction;
import kalina.compiler.instructions.DefaultConstructorInstruction;
import kalina.compiler.instructions.DoInstruction;
import kalina.compiler.instructions.ForInstruction;
import kalina.compiler.instructions.FunEndInstruction;
import kalina.compiler.instructions.IfInstruction;
import kalina.compiler.instructions.InitInstruction;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.SimpleInstruction;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.data.FunctionInfo;
import kalina.compiler.syntax.parser.data.FunctionTable;
import kalina.compiler.syntax.parser.data.IFunctionTable;
import kalina.compiler.syntax.parser.data.ILocalVariableTable;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.ITypeDictionary;
import kalina.compiler.syntax.parser.data.KDKMapper;
import kalina.compiler.syntax.parser.data.LocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.RuntimeConstantPool;
import kalina.compiler.syntax.parser.data.TypeAndIndex;
import kalina.compiler.syntax.parser.data.TypeDictionary;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class RecursiveDescentParser extends AbstractParser {
    private static final ILocalVariableTableFactory localVariableTableFactory = new LocalVariableTableFactory();
    private static final ITypeDictionary typeDictionary = new TypeDictionary();
    private static final Logger logger = Logger.getLogger(RecursiveDescentParser.class.getName());
    private final Map<String, RuntimeConstantPool> classInfo = new HashMap<>();

    public RecursiveDescentParser(IScanner scanner) {
        super(scanner);
    }

    @Override
    public ParseResult parse() throws ParseException {
        Optional<ClassBasicBlock> bb = parseStart();
        return new ParseResult(bb, ParsingStatus.SUCCESS);
    }

    private Optional<ClassBasicBlock> parseStart() throws ParseException {
        Token token = peekNextToken();
        if (token.getTag() == TokenTag.CLASS_TAG) {
            ClassBasicBlock classBasicBlock = parseClassDecl();
            Optional<ClassBasicBlock> next = parseStart();
            next.ifPresent(classBasicBlock::addAtTheEnd);
            return Optional.of(classBasicBlock);
        }
        if (isEnd()) {
            return Optional.empty();
        }
        throw new ParseException("Unexpected token: " + token.getTag());
    }

    private Optional<AbstractBasicBlock> parseClassEntry(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = peekNextToken();
        if (ParseUtils.isValidType(token, typeDictionary)) {
            BasicBlock bb = parseVarDecl(localVariableTable, functionTable, new Label(), new Label());
            Optional<AbstractBasicBlock> next = parseClassEntry(localVariableTable, functionTable, className);
            next.ifPresent(bb::addAtTheEnd);
            return Optional.of(bb);
        }
        if (token.getTag() == TokenTag.STATIC_TAG) {
            getNextToken();
            token = peekNextToken();
            if (token.getTag() == TokenTag.FUN_TAG) {
                return onFunDetected(localVariableTable, functionTable, className, true);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (token.getTag() == TokenTag.FUN_TAG) {
            return onFunDetected(localVariableTable, functionTable, className, false);
        }
        if (token.getTag() == TokenTag.BEGIN_TAG) {
            FunBasicBlock beginBlock = parseBegin(localVariableTable, functionTable, className);
            Optional<AbstractBasicBlock> next = parseClassEntry(localVariableTable, functionTable, className);
            next.ifPresent(beginBlock::addAtTheEnd);
            return Optional.of(beginBlock);
        }
        return Optional.empty();
    }

    private Optional<AbstractBasicBlock> onFunDetected(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String className,
            boolean isStatic) throws ParseException
    {
        FunBasicBlock funBasicBlock = parseFunDecl(isStatic, localVariableTable, functionTable, className);
        Optional<AbstractBasicBlock> next = parseClassEntry(localVariableTable, functionTable, className);
        next.ifPresent(funBasicBlock::addAtTheEnd);
        return Optional.of(funBasicBlock);
    }

    private BasicBlock parseVarDecl(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Label start, Label end) throws ParseException
    {
        return new BasicBlock(parseVarDeclInt(localVariableTable, functionTable, start, end));
    }

    private InitInstruction parseVarDeclInt(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Label start, Label end) throws ParseException
    {
        LHS lhs = parseLHS(localVariableTable);
        Token token = getNextToken();
        if (token.getTag() == TokenTag.ASSIGN_TAG) {
            RHS rhs = parseRHS(localVariableTable, functionTable);
            return new InitInstruction(lhs, Optional.of(rhs), start, end);
        }

        throw new IllegalArgumentException();
    }

    private LHS parseLHS(ILocalVariableTable localVariableTable) throws ParseException {
        List<VariableInfo> variableInfos = new ArrayList<>();
        Token token = getNextToken();
        Assert.assertValidType(token, typeDictionary);
        String type = token.getValue();
        Type convertedType = ParseUtils.convertRawType(type);
        variableInfos.add(parseSingleVariableDeclaration(convertedType, localVariableTable));
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableInfos.add(parseSingleVariableDeclaration(convertedType, localVariableTable));
        }

        return new LHS(variableInfos);
    }

    private VariableInfo parseSingleVariableDeclaration(Type type, ILocalVariableTable localVariableTable) throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();
        if (localVariableTable.hasVariable(name)) {
            throw new ParseException("Multiple variable declaration");
        }
        localVariableTable.addVariable(name, type);
        int index = localVariableTable.getTypeAndIndex(name).orElseThrow().getIndex();
        return new VariableInfo(name, index, type);
    }

    private RHS parseRHS(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        List<Expression> expressions = new ArrayList<>();
        expressions.add(parseExpression(localVariableTable, functionTable));
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            expressions.add(parseExpression(localVariableTable, functionTable));
        }

        return new RHS(expressions);
    }

    // todo arithmetic for strings and objects !!!!!
    // Это костыль - строки сейчас не могут складываться, как и вызовы функций
    private Expression parseExpression(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        if (peekNextToken().getTag() == TokenTag.NEW_TAG) {
            return parseNewInt(localVariableTable, functionTable);
        }
        if (peekNextToken().getTag() == TokenTag.STRING_LITERAL_TAG) {
            return new ValueExpression(getNextToken().getValue(), Type.getType(String.class));
        }
        if (peekNextToken().getTag() == TokenTag.IDENT_TAG) {
            Optional<FunctionInfo> infoO = functionTable.getFunctionInfo(peekNextToken().getValue());
            if (infoO.isPresent()) {
                FunctionInfo info = infoO.get();
                if (info.getReturnType().isPresent()) {
                    Type retType = info.getReturnType().get();
                    if (retType.equals(Type.getType(String.class))) {
                        return parseFunCallInt(localVariableTable, functionTable, getNextToken().getValue(), Optional.empty());
                    }
                    // если объект, то можно через точку ...
                }
            } else {
                Optional<TypeAndIndex> typeAndIndexO = localVariableTable.getTypeAndIndex(peekNextToken().getValue());
                if (typeAndIndexO.isPresent()) {
                    TypeAndIndex typeAndIndex = typeAndIndexO.get();
                    // если объект, то можно через точку ...
                }
            }
        }
        return parseAr(localVariableTable, functionTable);
    }

    private ArithmeticExpression parseAr(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        List<Term> terms = new ArrayList<>();
        List<ArithmeticOperation> operations = new ArrayList<>();
        Term term = parseTerm(localVariableTable, functionTable);
        terms.add(term);
        while (peekNextToken().getTag() == TokenTag.PLUS_TAG || peekNextToken().getTag() == TokenTag.MINUS_TAG) {
            Token token = getNextToken();
            if (token.getTag() == TokenTag.PLUS_TAG) {
                operations.add(ArithmeticOperation.PLUS);
            } else {
                operations.add(ArithmeticOperation.MINUS);
            }
            terms.add(parseTerm(localVariableTable, functionTable));
        }

        return new ArithmeticExpression(terms, operations);
    }

    private Term parseTerm(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        List<Factor> factors = new ArrayList<>();
        List<ArithmeticOperation> operations = new ArrayList<>();
        Factor factor = parseFactor(localVariableTable, functionTable);
        factors.add(factor);
        while (peekNextToken().getTag() == TokenTag.MUL_TAG || peekNextToken().getTag() == TokenTag.DIV_TAG) {
            Token token = getNextToken();
            if (token.getTag() == TokenTag.MUL_TAG) {
                operations.add(ArithmeticOperation.MULTIPLY);
            } else {
                operations.add(ArithmeticOperation.DIVIDE);
            }
            factors.add(parseFactor(localVariableTable, functionTable));
        }

        return new Term(factors, operations);
    }

    private Factor parseFactor(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        Token token = getNextToken();
        if (token.getTag() == TokenTag.NUMBER_TAG) {
            return Factor.createFactor(new ValueExpression(ParseUtils.getTrueValue(token), Type.INT_TYPE));
        }
        if (token.getTag() == TokenTag.LONG_NUMBER_TAG) {
            return Factor.createFactor(new ValueExpression(ParseUtils.getTrueValue(token), Type.LONG_TYPE));
        }
        if (token.getTag() == TokenTag.FLOAT_NUMBER_TAG) {
            return Factor.createFactor(new ValueExpression(ParseUtils.getTrueValue(token), Type.FLOAT_TYPE));
        }
        if (token.getTag() == TokenTag.DOUBLE_NUMBER_TAG) {
            return Factor.createFactor(new ValueExpression(ParseUtils.getTrueValue(token), Type.DOUBLE_TYPE));
        }
        if (token.getTag() == TokenTag.IDENT_TAG) {
            String name = token.getValue();
            if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
                FunCallExpression funCallExpression = parseFunCallInt(localVariableTable, functionTable, name, Optional.empty());
                return Factor.createFactor(funCallExpression);
            } else if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                getNextToken();
                Token methodName = getNextToken();
                Assert.assertTag(methodName, TokenTag.IDENT_TAG);
                Optional<TypeAndIndex> typeAndIndexO = localVariableTable.getTypeAndIndex(token.getValue());
                if (typeAndIndexO.isEmpty()) {
                    throw new ParseException("No variable declaration found");
                }
                String className = typeAndIndexO.get().getType().getClassName();
                RuntimeConstantPool pool = classInfo.get(className);
                if (pool == null) {
                    throw new ParseException("No variable declaration found");
                }
                IFunctionTable otherTable = pool.getFunctionTable();
                Expression methodCall = parseMethodCallExpr(localVariableTable, functionTable, otherTable, methodName.getValue(), typeAndIndexO);
                return Factor.createFactor(methodCall);
            }
            Optional<TypeAndIndex> typeAndIndexO = localVariableTable.getTypeAndIndex(token.getValue());
            if (typeAndIndexO.isEmpty()) {
                throw new ParseException("No variable declaration found");
            }
            TypeAndIndex typeAndIndex = typeAndIndexO.get();
            return Factor.createFactor(new VariableExpression(typeAndIndex.getIndex(), typeAndIndex.getType()));
        }
        if (token.getTag() == TokenTag.LPAREN_TAG) {
            Expression expression = parseAr(localVariableTable, functionTable);
            Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
            return Factor.createFactor(expression);
        }
        if (token.getTag() == TokenTag.MINUS_TAG) {
            Expression expression = parseFactor(localVariableTable, functionTable);
            return Factor.createNegateFactor(expression);
        }

        throw new ParseException("Unexpected token: " + token);
    }

    private FunBasicBlock parseFunDecl(
            boolean isStatic,
            ILocalVariableTable parent,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.FUN_TAG);
        token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();

        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        ILocalVariableTable localVariableTable = isStatic
                ? localVariableTableFactory.createLocalVariableTableForStatic()
                : localVariableTableFactory.createLocalVariableTableForNonStatic();
        localVariableTable.setParent(parent);
        List<TypeAndName> types = parseFunArgs(localVariableTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        Optional<Type> returnType;
        if (peekNextToken().getTag() == TokenTag.ARROW_TAG) {
            getNextToken();
            Token returnTypeToken = getNextToken();
            Assert.assertValidType(returnTypeToken, typeDictionary);
            returnType = Optional.of(ParseUtils.convertRawType(returnTypeToken.getValue()));
        } else {
            returnType = Optional.empty();
        }
        FunctionInfo functionInfo = new FunctionInfo(types, returnType, className, false, isStatic);
        functionTable.addFunction(name, functionInfo);

        FunBasicBlock funBasicBlock = new FunBasicBlock(name, types, returnType, isStatic);

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        Optional<AbstractBasicBlock> entry = parseFunEntry(localVariableTable, returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        entry.ifPresent(funBasicBlock::addAtTheEnd);

        return funBasicBlock;
    }

    private List<TypeAndName> parseFunArgs(ILocalVariableTable localVariableTable) {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        Token token = getNextToken();
        Assert.assertValidType(token, typeDictionary);
        Type convertedType = ParseUtils.convertRawType(token.getValue());
        List<TypeAndName> typeAndNames = parseFunArgsInt(localVariableTable, convertedType);
        List<TypeAndName> result = new ArrayList<>(typeAndNames);
        while (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            getNextToken();
            token = getNextToken();
            Assert.assertValidType(token, typeDictionary);
            Type converted = ParseUtils.convertRawType(token.getValue());
            typeAndNames  = parseFunArgsInt(localVariableTable, converted);
            result.addAll(typeAndNames);
        }

        return result;
    }

    private List<TypeAndName> parseFunArgsInt(ILocalVariableTable localVariableTable, Type type) {
        List<TypeAndName> typeAndNames = new ArrayList<>();
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        typeAndNames.add(new TypeAndName(type, token.getValue()));
        localVariableTable.addVariable(token.getValue(), type);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            token = getNextToken();
            Assert.assertTag(token, TokenTag.IDENT_TAG);
            localVariableTable.addVariable(token.getValue(), type);
            typeAndNames.add(new TypeAndName(type, token.getValue()));
        }
        return typeAndNames;
    }

    private AbstractBasicBlock parseStaticMethodCall(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String ownerClassName) throws ParseException
    {
        return parseMethodCall(localVariableTable, functionTable, ownerClassName, Optional.empty());
    }

    private AbstractBasicBlock parseMethodCall(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String className,
            Optional<String> varName) throws ParseException
    {
        RuntimeConstantPool pool = classInfo.get(className);
        if (pool == null) {
            throw new ParseException("Unknown class references");
        }
        String methodName = getNextToken().getValue();
        IFunctionTable otherFunctionTable = pool.getFunctionTable();
        Optional<TypeAndIndex> typeAndIndexO = Optional.empty();
        if (varName.isPresent()) {
            typeAndIndexO = localVariableTable.getTypeAndIndex(varName.get());
            if (typeAndIndexO.isEmpty()) {
                throw new ParseException("No variable declaration found");
            }
        }
        return parseMethodCallBB(localVariableTable, functionTable, otherFunctionTable, methodName, typeAndIndexO);
    }

    private Optional<AbstractBasicBlock> parseFunEntry(
            ILocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = peekNextToken();
        if (ParseUtils.isValidType(token, typeDictionary)) {
//            getNextToken();
//            if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
//                String ownerName = token.getValue();
//                getNextToken();
//                AbstractBasicBlock staticMethodCall = parseStaticMethodCall(localVariableTable, functionTable, ownerName);
//                Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
//                next.ifPresent(staticMethodCall::addAtTheEnd);
//                return Optional.of(staticMethodCall);
//            }

            BasicBlock varDecl = parseVarDecl(localVariableTable, functionTable, new Label(), new Label());
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(varDecl::addAtTheEnd);
            return Optional.of(varDecl);
        }
        if (token.getTag() == TokenTag.FUN_TAG) {
            FunBasicBlock funBasicBlock = parseFunDecl(false, localVariableTable, functionTable, className);
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(funBasicBlock::addAtTheEnd);
            return Optional.of(funBasicBlock);
        }
        if (token.getTag() == TokenTag.IDENT_TAG) {
            getNextToken();
            String identName = token.getValue();
            if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
                AbstractBasicBlock funCallBlock = parseFunCall(localVariableTable, functionTable, token.getValue(), Optional.empty());
                Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
                next.ifPresent(funCallBlock::addAtTheEnd);
                return Optional.of(funCallBlock);
            } else if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                getNextToken();
                AbstractBasicBlock methodCallBlock = parseMethodCall(localVariableTable, functionTable, token.getValue(), Optional.of(identName));
                Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
                next.ifPresent(methodCallBlock::addAtTheEnd);
                return Optional.of(methodCallBlock);
            } else {
                AbstractBasicBlock assign = parseAssign(localVariableTable, functionTable, token.getValue());
                Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
                next.ifPresent(assign::addAtTheEnd);
                return Optional.of(assign);
            }
        }
        if (token.getTag() == TokenTag.IF_TAG) {
            AbstractBasicBlock ifStmt = parseBrStmt(localVariableTable, returnType, functionTable, className, TokenTag.IF_TAG);
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(ifStmt::addAtTheEnd);
            return Optional.of(ifStmt);
        }
        if (token.getTag() == TokenTag.FOR_TAG) {
            AbstractBasicBlock forStmt = parseForStmt(localVariableTable, returnType, functionTable, className);
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(forStmt::addAtTheEnd);
            return Optional.of(forStmt);
        }
        if (token.getTag() == TokenTag.DO_TAG) {
            AbstractBasicBlock doStmt = parseDoStmt(localVariableTable, returnType, functionTable, className);
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(doStmt::addAtTheEnd);
            return Optional.of(doStmt);
        }
        if (token.getTag() == TokenTag.NEW_TAG) {
            AbstractBasicBlock bb = parseNew(localVariableTable, functionTable);
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(bb::addAtTheEnd);
            return Optional.of(bb);
        }
        if (token.getTag() == TokenTag.RETURN_TAG) {
            getNextToken();
            return Optional.of(onReturnDetected(localVariableTable, functionTable, returnType));
        }
        if (token.getTag() == TokenTag.RBRACE_TAG) {
            return Optional.of(new BasicBlock(new FunEndInstruction(Optional.empty())));
        }

        throw new IllegalArgumentException();
    }

    private AbstractBasicBlock parseMethodCallBB(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            IFunctionTable otherFunctionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        return new BasicBlock(parseMethodCall(localVariableTable, functionTable, otherFunctionTable, funName, typeAndIndex));
    }

    private Instruction parseMethodCall(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            IFunctionTable otherFunctionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        if (otherFunctionTable.getFunctionInfo(funName).isPresent()) {
            return new SimpleInstruction(parseMethodCallExpr(localVariableTable, functionTable, otherFunctionTable, funName, typeAndIndex));
        }
        logger.severe("No function definition found for fun " + funName);
        throw new ParseException("No function definition found for fun " + funName);
    }

    private Expression parseMethodCallExpr(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            IFunctionTable otherFunctionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
        Optional<FunctionInfo> functionInfo = otherFunctionTable.getFunctionInfo(funName);
        if (functionInfo.isEmpty()) {
            throw new RuntimeException("No function definition found");
        }

        return new FunCallExpression(funName, expressions, functionInfo.get(), typeAndIndex.map(TypeAndIndex::getIndex));
    }

    private Instruction parseAction(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable) throws ParseException
    {
        String value = getNextToken().getValue();
        if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
            return getFunCallInstruction(localVariableTable, functionTable, value, Optional.empty());
        } else {
            return getAssignInstruction(localVariableTable, functionTable, value);
        }
    }

    private Optional<AbstractBasicBlock> parseScope(
            ILocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = peekNextToken();
        if (ParseUtils.isValidType(token, typeDictionary)) {
//            getNextToken();
//            if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
//                String ownerName = token.getValue();
//                getNextToken();
//                AbstractBasicBlock staticMethodCall = parseStaticMethodCall(localVariableTable, functionTable, ownerName);
//                Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
//                next.ifPresent(staticMethodCall::addAtTheEnd);
//                return Optional.of(staticMethodCall);
//            }
            BasicBlock varDecl = parseVarDecl(localVariableTable, functionTable, new Label(), new Label());
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(varDecl::addAtTheEnd);
            return Optional.of(varDecl);
        }
        if (token.getTag() == TokenTag.FUN_TAG) {
            FunBasicBlock funBasicBlock = parseFunDecl(false, localVariableTable, functionTable, className);
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(funBasicBlock::addAtTheEnd);
            return Optional.of(funBasicBlock);
        }
        if (token.getTag() == TokenTag.IDENT_TAG) {
            getNextToken();
            if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
                AbstractBasicBlock funCallBlock = parseFunCall(localVariableTable, functionTable, token.getValue(), Optional.empty());
                Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
                next.ifPresent(funCallBlock::addAtTheEnd);
                return Optional.of(funCallBlock);
            } else {
                AbstractBasicBlock assign = parseAssign(localVariableTable, functionTable, token.getValue());
                Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
                next.ifPresent(assign::addAtTheEnd);
                return Optional.of(assign);
            }
        }
        if (token.getTag() == TokenTag.IF_TAG) {
            AbstractBasicBlock ifStmt = parseBrStmt(localVariableTable, returnType, functionTable, className, TokenTag.IF_TAG);
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(ifStmt::addAtTheEnd);
            return Optional.of(ifStmt);
        }
        if (token.getTag() == TokenTag.FOR_TAG) {
            AbstractBasicBlock forStmt = parseForStmt(localVariableTable, returnType, functionTable, className);
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(forStmt::addAtTheEnd);
            return Optional.of(forStmt);
        }
        if (token.getTag() == TokenTag.DO_TAG) {
            AbstractBasicBlock doStmt = parseDoStmt(localVariableTable, returnType, functionTable, className);
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(doStmt::addAtTheEnd);
            return Optional.of(doStmt);
        }
        if (token.getTag() == TokenTag.NEW_TAG) {
            AbstractBasicBlock bb = parseNew(localVariableTable, functionTable);
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(bb::addAtTheEnd);
            return Optional.of(bb);
        }
        if (token.getTag() == TokenTag.RETURN_TAG) {
            getNextToken();
            return Optional.of(onReturnDetected(localVariableTable, functionTable, returnType));
        }
        if (token.getTag() == TokenTag.RBRACE_TAG) {
            return Optional.empty();
        }

        throw new IllegalArgumentException();
    }

    private AbstractBasicBlock onReturnDetected(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Optional<Type> returnType) throws ParseException
    {
        if (returnType.isEmpty() || returnType.get().getSort() == Type.VOID) {
            new BasicBlock(new FunEndInstruction(Optional.empty()));
        }
        Type type = returnType.get();
        Expression returnValue = parseExpression(localVariableTable, functionTable);
        if (type.getSort() != returnValue.getType().getSort()) {
            logger.warning("Return type mismatch: expected " + type + "got " + returnValue.getType());
        }
        return new BasicBlock(new FunEndInstruction(Optional.of(new ReturnValueInfo(type, returnValue))));
    }

    private ClassBasicBlock parseClassDecl() throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.CLASS_TAG);
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();
        typeDictionary.addType(name);

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        ClassBasicBlock classBasicBlock = new ClassBasicBlock(new DefaultConstructorInstruction(name));

        ILocalVariableTable localVariableTable = localVariableTableFactory.createLocalVariableTableForStatic();
        IFunctionTable functionTable = new FunctionTable();

        Optional<AbstractBasicBlock> next = parseClassEntry(localVariableTable, functionTable, name);

        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        next.ifPresent(classBasicBlock::addAtTheEnd);
        RuntimeConstantPool pool = new RuntimeConstantPool(name, functionTable);
        classInfo.put(name, pool);

        return classBasicBlock;
    }

    private FunBasicBlock parseBegin(ILocalVariableTable parent, IFunctionTable functionTable, String className) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.BEGIN_TAG);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        Type argsType = Type.getType(String[].class);
        FunBasicBlock begin = new FunBasicBlock("main", List.of(new TypeAndName(argsType, "args")), Optional.empty(), true);
        ILocalVariableTable localVariableTable = localVariableTableFactory.createLocalVariableTableForNonStatic();
        localVariableTable.setParent(parent);
        Optional<AbstractBasicBlock> entry = parseFunEntry(localVariableTable, Optional.empty(), functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        entry.ifPresent(begin::addAtTheEnd);

        return begin;
    }

    private LHS parseVariableAssign(ILocalVariableTable localVariableTable, String firstVarName) throws ParseException {
        List<VariableInfo> variableInfos = new ArrayList<>();
        VariableInfo variableInfo = getVariableInfo(localVariableTable, firstVarName);
        variableInfos.add(variableInfo);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableInfos.add(parseSingleVariableAssign(localVariableTable));
        }

        return new LHS(variableInfos);
    }

    private VariableInfo parseSingleVariableAssign(ILocalVariableTable localVariableTable) throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();
        return getVariableInfo(localVariableTable, name);
    }

    private VariableInfo getVariableInfo(ILocalVariableTable localVariableTable, String name) throws ParseException {
        Optional<TypeAndIndex> typeAndIndexO = localVariableTable.getTypeAndIndex(name);
        if (typeAndIndexO.isEmpty()) {
            throw new ParseException("Variable was not declared: " + name);
        }
        TypeAndIndex typeAndIndex = typeAndIndexO.get();
        return new VariableInfo(name, typeAndIndex.getIndex(), typeAndIndex.getType());
    }

    private AbstractBasicBlock parseAssign(ILocalVariableTable localVariableTable, IFunctionTable functionTable, String firstVarName) throws ParseException {
        return new BasicBlock(getAssignInstruction(localVariableTable, functionTable, firstVarName));
    }

    private Instruction getAssignInstruction(ILocalVariableTable localVariableTable, IFunctionTable functionTable, String firstVarName) throws ParseException {
        LHS lhs = parseVariableAssign(localVariableTable, firstVarName);
        Assert.assertTag(getNextToken(), TokenTag.ASSIGN_TAG);
        RHS rhs = parseRHS(localVariableTable, functionTable);
        return new AssignInstruction(lhs, rhs);
    }

    private AbstractBasicBlock parseFunCall(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        return new BasicBlock(getFunCallInstruction(localVariableTable, functionTable, funName, typeAndIndex));
    }

    private Instruction getFunCallInstruction(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        if (functionTable.getFunctionInfo(funName).isPresent()) {
            return new SimpleInstruction(parseFunCallInt(localVariableTable, functionTable, funName, typeAndIndex));
        } else {
            Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
            List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
            Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
            Instruction instruction;
            try {
                instruction = KDKMapper.get(funName).getConstructor(List.class).newInstance(expressions);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                logger.severe("Cannot call reflective constructor: " + e.getLocalizedMessage());
                throw new ParseException("Error");
            }

            return instruction;
        }
    }

    private FunCallExpression parseFunCallInt(
            ILocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
        Optional<FunctionInfo> functionInfo = functionTable.getFunctionInfo(funName);
        if (functionInfo.isEmpty()) {
            throw new RuntimeException("No function definition found");
        }
        FunctionInfo info = functionInfo.get();
        Optional<Integer> index = Optional.empty();
        if (typeAndIndex.isPresent()) {
            index = Optional.of(typeAndIndex.get().getIndex());
        } else if (!info.isStatic()){
            index = Optional.of(0);
        }

        return new FunCallExpression(funName, expressions, info, index);
    }

    private List<Expression> parseFunArgs(ILocalVariableTable localVariableTable, IFunctionTable functionTable)
            throws ParseException
    {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        List<Expression> expressions = new ArrayList<>();
        expressions.add(parseExpression(localVariableTable, functionTable));
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            expressions.add(parseExpression(localVariableTable, functionTable));
        }

        return expressions;
    }

    private AbstractBasicBlock parseBrStmt(
            ILocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className,
            TokenTag brTag) throws ParseException
    {
        Assert.assertTag(getNextToken(), brTag);
        CondExpression cond = parseCond(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        Optional<AbstractBasicBlock> entry = parseScope(localVariableTable, returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        Optional<AbstractBasicBlock> elseEntry = Optional.empty();
        if (peekNextToken().getTag() == TokenTag.ELSE_TAG) {
            getNextToken();
            Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
            elseEntry = parseScope(localVariableTable, returnType, functionTable, className);
            Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        } else if (peekNextToken().getTag() == TokenTag.ELIF_TAG) {
            elseEntry = Optional.of(parseBrStmt(localVariableTable, returnType, functionTable, className, TokenTag.ELIF_TAG));
        }

        return new BasicBlock(new IfInstruction(cond, entry, elseEntry));
    }

    // todo
    private CondExpression parseCond(ILocalVariableTable localVariableTable, IFunctionTable functionTable)
            throws ParseException
    {
        return parseCondExpr(localVariableTable, functionTable);
    }

    // todo
    private CondExpression parseCondExpr(ILocalVariableTable localVariableTable, IFunctionTable functionTable)
            throws ParseException
    {
        Expression expression = parseExpression(localVariableTable, functionTable);
        if (expression.getType().equals(Type.BOOLEAN_TYPE)) {
            return new CondExpression(List.of(expression), List.of());
        }
        List<Expression> expressions = new ArrayList<>();
        expressions.add(expression);
        List<ComparisonOperation> operations = new ArrayList<>();
        Token token = getNextToken();
        ComparisonOperation operation = ParseUtils.getComparisonOperation(token);
        operations.add(operation);
        expressions.add(parseExpression(localVariableTable, functionTable));
        if (ParseUtils.isComparisonOperation(peekNextToken())) {
            operations.add(ParseUtils.getComparisonOperation(getNextToken()));
            expressions.add(parseExpression(localVariableTable, functionTable));
        }

        return new CondExpression(expressions, operations);
    }

    private AbstractBasicBlock parseDoStmt(
            ILocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.DO_TAG);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        Optional<AbstractBasicBlock> entry = parseScope(localVariableTable, returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        Assert.assertTag(getNextToken(), TokenTag.WHILE_TAG);
        CondExpression cond = parseCond(localVariableTable, functionTable);
        return new BasicBlock(new DoInstruction(entry, cond));
    }

    private AbstractBasicBlock parseForStmt(
            ILocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.FOR_TAG);
        Optional<InitInstruction> varDecl;
        if (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            varDecl = Optional.empty();
        } else {
            varDecl = Optional.of(parseVarDeclInt(localVariableTable, functionTable, new Label(), new Label()));
        }
        Assert.assertTag(getNextToken(), TokenTag.SEMICOLON_TAG);
        Optional<CondExpression> cond;
        if (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            cond = Optional.empty();
        } else {
            cond = Optional.of(parseCondExpr(localVariableTable, functionTable));
        }
        Assert.assertTag(getNextToken(), TokenTag.SEMICOLON_TAG);
        Optional<Instruction> action;
        if (peekNextToken().getTag() == TokenTag.LBRACE_TAG) {
            action = Optional.empty();
        } else {
            action = Optional.of(parseAction(localVariableTable, functionTable));
        }
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        Optional<AbstractBasicBlock> entry = parseScope(localVariableTable, returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return new BasicBlock(new ForInstruction(varDecl, cond, action, entry));
    }

    private AbstractBasicBlock parseNew(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        return new BasicBlock(new SimpleInstruction(parseNewInt(localVariableTable, functionTable)));
    }

    private Expression parseNewInt(ILocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.NEW_TAG);
        Token token = getNextToken();
        Assert.assertValidType(token, typeDictionary);
        String className = token.getValue();
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> arguments = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ObjectCreationExpression(className, arguments);
    }
}
