package kalina.compiler.syntax.parser;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import kalina.compiler.expressions.VariableNameAndIndex;
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
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.FunctionInfo;
import kalina.compiler.syntax.parser.data.FunctionTable;
import kalina.compiler.syntax.parser.data.IFunctionTable;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.ITypeDictionary;
import kalina.compiler.syntax.parser.data.KDKMapper;
import kalina.compiler.syntax.parser.data.LocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.RuntimeConstantPool;
import kalina.compiler.syntax.parser.data.TypeAndIndex;
import kalina.compiler.syntax.parser.data.TypeDictionary;
import kalina.compiler.syntax.parser.data.VariableInfo;
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

    private static final String UNDECLARED_VARIABLE_ERROR_MESSAGE = "No variable declaration found";

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
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = peekNextToken();
        if (ParseUtils.isValidDeclarationType(token, typeDictionary)) {
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
            AbstractLocalVariableTable localVariableTable,
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
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Label start, Label end) throws ParseException
    {
        return new BasicBlock(parseVarDeclInt(localVariableTable, functionTable, start, end));
    }

    private InitInstruction parseVarDeclInt(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Label start, Label end) throws ParseException
    {
        LHS lhs = parseLHS(localVariableTable);
        Token token = peekNextToken();
        Optional<RHS> rhs;
        if (token.getTag() == TokenTag.ASSIGN_TAG) {
            getNextToken();
            rhs = Optional.of(parseRHS(localVariableTable, functionTable, lhs));
        } else {
            rhs = Optional.empty();
        }

        return new InitInstruction(lhs, rhs, start, end);
    }

    private LHS parseLHS(AbstractLocalVariableTable localVariableTable) throws ParseException {
        List<VariableNameAndIndex> variableInfos = new ArrayList<>();
        Token token = getNextToken();
        Assert.assertValidDeclarationType(token, typeDictionary);
        String type = token.getValue();
        Type convertedType = ParseUtils.convertRawType(type);
        variableInfos.add(parseSingleVariableDeclaration(convertedType, localVariableTable));
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableInfos.add(parseSingleVariableDeclaration(convertedType, localVariableTable));
        }

        return new LHS(variableInfos, convertedType);
    }

    private VariableNameAndIndex parseSingleVariableDeclaration(Type type, AbstractLocalVariableTable localVariableTable) throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();
        if (localVariableTable.hasVariable(name)) {
            throw new ParseException("Multiple variable declaration");
        }
        localVariableTable.addVariable(name, type);
        int index = localVariableTable.findVariable(name).orElseThrow().getIndex();
        return new VariableNameAndIndex(name, index);
    }

    private RHS parseRHS(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable, LHS lhs) throws ParseException {
        List<Expression> expressions = new ArrayList<>();
        Type type = lhs.getType();
        Expression expression = parseExpression(localVariableTable, functionTable);
        Assert.assertTypesCompatible(type, expression.getType());
        expressions.add(expression);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            Expression expr = parseExpression(localVariableTable, functionTable);
            Assert.assertTypesCompatible(type, expr.getType());
            expressions.add(expr);
        }

        if (expressions.size() != lhs.size()) {
            throw new ParseException("Different number of variables and values in assign");
        }
        return new RHS(expressions);
    }

    // todo arithmetic for strings and objects !!!!!
    private Expression parseExpression(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        if (peekNextToken().getTag() == TokenTag.NEW_TAG) {
            return parseNewInt(localVariableTable, functionTable);
        }
        if (peekNextToken().getTag() == TokenTag.STRING_LITERAL_TAG) {
            return new ValueExpression(getNextToken().getValue(), Type.getType(String.class));
        }
        if (peekNextToken().getTag() ==TokenTag.BOOL_VALUE_TAG) {
            return new ValueExpression(ParseUtils.getTrueValue(getNextToken()), Type.BOOLEAN_TYPE);
        }
        return parseAr(localVariableTable, functionTable);
    }

    private ArithmeticExpression parseAr(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
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

    private Term parseTerm(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
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

    private Factor parseFactor(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
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
                Optional<TypeAndIndex> typeAndIndexO = localVariableTable.findVariable(name);
                if (typeAndIndexO.isEmpty()) {
                    throw new ParseException(String.format("%s: %s", UNDECLARED_VARIABLE_ERROR_MESSAGE, name));
                }
                String className = typeAndIndexO.get().getType().getClassName();
                RuntimeConstantPool pool = classInfo.get(className);
                if (pool == null) {
                    throw new ParseException("Cannot determine class for variable " + name);
                }
                IFunctionTable otherTable = pool.getFunctionTable();
                Expression methodCall = parseMethodCallExpr(localVariableTable, functionTable, otherTable, methodName.getValue(), typeAndIndexO);
                return Factor.createFactor(methodCall);
            }
            Optional<TypeAndIndex> typeAndIndexO = localVariableTable.findVariable(name);
            if (typeAndIndexO.isEmpty()) {
                throw new ParseException(String.format("%s: %s", UNDECLARED_VARIABLE_ERROR_MESSAGE, name));
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
            AbstractLocalVariableTable parent,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.FUN_TAG);
        token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();

        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        AbstractLocalVariableTable localVariableTable = isStatic
                ? localVariableTableFactory.createLocalVariableTableForStatic()
                : localVariableTableFactory.createLocalVariableTableForNonStatic();
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

    private List<TypeAndName> parseFunArgs(AbstractLocalVariableTable localVariableTable) throws ParseException {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        Token token = getNextToken();
        Assert.assertValidDeclarationType(token, typeDictionary);
        Type convertedType = ParseUtils.convertRawType(token.getValue());
        List<TypeAndName> typeAndNames = parseFunArgsInt(localVariableTable, convertedType);
        List<TypeAndName> result = new ArrayList<>(typeAndNames);
        while (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            getNextToken();
            token = getNextToken();
            Assert.assertValidDeclarationType(token, typeDictionary);
            Type converted = ParseUtils.convertRawType(token.getValue());
            typeAndNames  = parseFunArgsInt(localVariableTable, converted);
            result.addAll(typeAndNames);
        }

        return result;
    }

    private List<TypeAndName> parseFunArgsInt(AbstractLocalVariableTable localVariableTable, Type type) throws ParseException {
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
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String ownerClassName) throws ParseException
    {
        return parseMethodCall(localVariableTable, functionTable, ownerClassName, Optional.empty(), true);
    }

    private AbstractBasicBlock parseMethodCall(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String className,
            Optional<String> classRef,
            boolean isStaticMethodCall) throws ParseException
    {
        RuntimeConstantPool pool = classInfo.get(className);
        if (pool == null) {
            throw new ParseException("Unknown class references");
        }
        String methodName = getNextToken().getValue();
        IFunctionTable otherFunctionTable = pool.getFunctionTable();
        Optional<TypeAndIndex> typeAndIndexO = Optional.empty();
        if (classRef.isPresent()) {
            typeAndIndexO = localVariableTable.findVariable(classRef.get());
            if (typeAndIndexO.isEmpty()) {
                throw new ParseException(String.format("%s: %s", UNDECLARED_VARIABLE_ERROR_MESSAGE, classRef.get()));
            }
        }
        return parseMethodCallBB(localVariableTable, functionTable, otherFunctionTable, methodName, typeAndIndexO, isStaticMethodCall);
    }

    private AbstractBasicBlock onTypeDetected(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Label start, Label end) throws ParseException
    {
        String rawType = getNextToken().getValue();
        Type convertedType = ParseUtils.convertRawType(rawType);
        if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
            if (convertedType.getSort() != Type.OBJECT) {
                throw new ParseException("Not an object. Cannot call static method on " + rawType);
            }
            getNextToken();
            return parseStaticMethodCall(localVariableTable, functionTable, rawType);
        }

        return new BasicBlock(parseVarDeclIntWithKnownType(localVariableTable, functionTable, start, end, convertedType));
    }

    private InitInstruction parseVarDeclIntWithKnownType(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Label start, Label end, Type type) throws ParseException
    {
        LHS lhs = parseLHSWithKnownType(localVariableTable, type);
        Token token = peekNextToken();
        Optional<RHS> rhs;
        if (token.getTag() == TokenTag.ASSIGN_TAG) {
            getNextToken();
            rhs = Optional.of(parseRHS(localVariableTable, functionTable, lhs));
        } else {
            rhs = Optional.empty();
        }

        return new InitInstruction(lhs, rhs, start, end);
    }

    private LHS parseLHSWithKnownType(AbstractLocalVariableTable localVariableTable, Type convertedType) throws ParseException {
        List<VariableNameAndIndex> variableInfos = new ArrayList<>();
        variableInfos.add(parseSingleVariableDeclaration(convertedType, localVariableTable));
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableInfos.add(parseSingleVariableDeclaration(convertedType, localVariableTable));
        }

        return new LHS(variableInfos, convertedType);
    }

    private Optional<AbstractBasicBlock> parseFunEntry(
            AbstractLocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = peekNextToken();
        if (ParseUtils.isValidDeclarationType(token, typeDictionary)) {
            AbstractBasicBlock bb = onTypeDetected(localVariableTable, functionTable, new Label(), new Label());
            Optional<AbstractBasicBlock> next = parseFunEntry(localVariableTable, returnType, functionTable, className);
            next.ifPresent(bb::addAtTheEnd);
            return Optional.of(bb);
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
                AbstractBasicBlock methodCallBlock = parseMethodCall(
                        localVariableTable,
                        functionTable,
                        getVariableType(localVariableTable, identName).map(Type::getClassName)
                                .orElseThrow(() -> new ParseException(String.format("%s: %s", UNDECLARED_VARIABLE_ERROR_MESSAGE, identName))),
                        Optional.of(identName),
                        false);
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

    private Optional<Type> getVariableType(AbstractLocalVariableTable localVariableTable, String name) {
        return localVariableTable.findVariable(name).map(TypeAndIndex::getType);
    }

    private AbstractBasicBlock parseMethodCallBB(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            IFunctionTable otherFunctionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex,
            boolean isStaticMethodCall) throws ParseException
    {
        return new BasicBlock(parseMethodCall(localVariableTable, functionTable, otherFunctionTable, funName, typeAndIndex, isStaticMethodCall));
    }

    private Instruction parseMethodCall(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            IFunctionTable otherFunctionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex,
            boolean isStaticMethodCall) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
        Optional<FunctionInfo> functionInfoO = otherFunctionTable.getFunctionInfo(funName, ParseUtils.expressionsToTypes(expressions));
        if (functionInfoO.isPresent()) {
            FunctionInfo functionInfo = functionInfoO.get();
            if (isStaticMethodCall && !functionInfo.isStatic()) {
                throw new ParseException(
                        String.format("Calling non static method %s of class %s in a static way", funName, functionInfo.getOwnerClass()));
            }

            Expression funCallExpr =
                    new FunCallExpression(funName, expressions, functionInfo, typeAndIndex.map(TypeAndIndex::getIndex));
            return new SimpleInstruction(funCallExpr);
        }
        throw new ParseException("No function definition found for fun " + funName);
    }

    private Expression parseMethodCallExpr(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            IFunctionTable otherFunctionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
        Optional<FunctionInfo> functionInfo = otherFunctionTable.getFunctionInfo(funName, ParseUtils.expressionsToTypes(expressions));
        if (functionInfo.isEmpty()) {
            throw new RuntimeException("No function definition found");
        }

        return new FunCallExpression(funName, expressions, functionInfo.get(), typeAndIndex.map(TypeAndIndex::getIndex));
    }

    private Instruction parseAction(
            AbstractLocalVariableTable localVariableTable,
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
            AbstractLocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Token token = peekNextToken();
        if (ParseUtils.isValidDeclarationType(token, typeDictionary)) {
            AbstractBasicBlock bb = onTypeDetected(localVariableTable, functionTable, new Label(), new Label());
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(bb::addAtTheEnd);
            return Optional.of(bb);
        }
        if (token.getTag() == TokenTag.FUN_TAG) {
            FunBasicBlock funBasicBlock = parseFunDecl(false, localVariableTable, functionTable, className);
            Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
            next.ifPresent(funBasicBlock::addAtTheEnd);
            return Optional.of(funBasicBlock);
        }
        if (token.getTag() == TokenTag.IDENT_TAG) {
            String identName = token.getValue();
            getNextToken();
            if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
                AbstractBasicBlock funCallBlock = parseFunCall(localVariableTable, functionTable, token.getValue(), Optional.empty());
                Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
                next.ifPresent(funCallBlock::addAtTheEnd);
                return Optional.of(funCallBlock);
            } else if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                getNextToken();
                AbstractBasicBlock methodCallBlock = parseMethodCall(
                        localVariableTable,
                        functionTable,
                        getVariableType(localVariableTable, identName).map(Type::getClassName)
                                .orElseThrow(() -> new ParseException(String.format("%s: %s", UNDECLARED_VARIABLE_ERROR_MESSAGE, identName))),
                        Optional.of(identName),
                        false);
                Optional<AbstractBasicBlock> next = parseScope(localVariableTable, returnType, functionTable, className);
                next.ifPresent(methodCallBlock::addAtTheEnd);
                return Optional.of(methodCallBlock);
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
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            Optional<Type> returnType) throws ParseException
    {
        if (returnType.isEmpty() || returnType.get().getSort() == Type.VOID) {
            return new BasicBlock(new FunEndInstruction(Optional.empty()));
        }
        Type type = returnType.get();
        Expression returnValue = parseExpression(localVariableTable, functionTable);
        Assert.assertTypesCompatible(type, returnValue.getType());
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

        AbstractLocalVariableTable localVariableTable = localVariableTableFactory.createLocalVariableTableForStatic();
        IFunctionTable functionTable = new FunctionTable();

        Optional<AbstractBasicBlock> next = parseClassEntry(localVariableTable, functionTable, name);

        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        next.ifPresent(classBasicBlock::addAtTheEnd);
        RuntimeConstantPool pool = new RuntimeConstantPool(name, functionTable);
        classInfo.put(name, pool);

        return classBasicBlock;
    }

    private FunBasicBlock parseBegin(AbstractLocalVariableTable parent, IFunctionTable functionTable, String className) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.BEGIN_TAG);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        AbstractLocalVariableTable localVariableTable = localVariableTableFactory.createLocalVariableTableForStatic();
        Type argsType = Type.getType(String[].class);
        String name = "args";
        localVariableTable.addVariable(name, argsType);
        FunBasicBlock begin = new FunBasicBlock("main", List.of(new TypeAndName(argsType, "args")), Optional.empty(), true);

        Optional<AbstractBasicBlock> entry = parseFunEntry(localVariableTable, Optional.empty(), functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        entry.ifPresent(begin::addAtTheEnd);

        return begin;
    }

    private LHS parseVariableAssign(AbstractLocalVariableTable localVariableTable, String firstVarName) throws ParseException {
        List<VariableInfo> variableInfos = new ArrayList<>();
        VariableInfo variableInfo = getVariableInfo(localVariableTable, firstVarName);
        variableInfos.add(variableInfo);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableInfos.add(parseSingleVariableAssign(localVariableTable));
        }

        return new LHS(variableInfos.stream()
                .map(info -> new VariableNameAndIndex(info.getName(), info.getIndex()))
                .collect(Collectors.toList()),
                variableInfos.stream().findFirst().orElseThrow().getType());
    }

    private VariableInfo parseSingleVariableAssign(AbstractLocalVariableTable localVariableTable) throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();
        return getVariableInfo(localVariableTable, name);
    }

    private VariableInfo getVariableInfo(AbstractLocalVariableTable localVariableTable, String name) throws ParseException {
        Optional<TypeAndIndex> typeAndIndexO = localVariableTable.findVariable(name);
        if (typeAndIndexO.isEmpty()) {
            throw new ParseException("Variable was not declared: " + name);
        }
        TypeAndIndex typeAndIndex = typeAndIndexO.get();
        return new VariableInfo(name, typeAndIndex.getIndex(), typeAndIndex.getType());
    }

    private AbstractBasicBlock parseAssign(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable, String firstVarName) throws ParseException {
        return new BasicBlock(getAssignInstruction(localVariableTable, functionTable, firstVarName));
    }

    private Instruction getAssignInstruction(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable, String firstVarName) throws ParseException {
        LHS lhs = parseVariableAssign(localVariableTable, firstVarName);
        Assert.assertTag(getNextToken(), TokenTag.ASSIGN_TAG);
        RHS rhs = parseRHS(localVariableTable, functionTable, lhs);
        return new AssignInstruction(lhs, rhs);
    }

    private AbstractBasicBlock parseFunCall(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        return new BasicBlock(getFunCallInstruction(localVariableTable, functionTable, funName, typeAndIndex));
    }

    private Instruction getFunCallInstruction(
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
        if (functionTable.getFunctionInfo(funName, ParseUtils.expressionsToTypes(expressions)).isPresent()) {
            return new SimpleInstruction(parseFunCallInt(localVariableTable, functionTable, funName, typeAndIndex));
        } else {
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
            AbstractLocalVariableTable localVariableTable,
            IFunctionTable functionTable,
            String funName,
            Optional<TypeAndIndex> typeAndIndex) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> expressions = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
        Optional<FunctionInfo> functionInfo = functionTable.getFunctionInfo(funName, ParseUtils.expressionsToTypes(expressions));
        if (functionInfo.isEmpty()) {
            throw new ParseException("No function definition found");
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

    private List<Expression> parseFunArgs(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable)
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
            AbstractLocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className,
            TokenTag brTag) throws ParseException
    {
        Assert.assertTag(getNextToken(), brTag);
        CondExpression cond = parseCond(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        Optional<AbstractBasicBlock> entry = parseScope(
                localVariableTableFactory.createChildLocalVariableTable(localVariableTable),
                returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        Optional<AbstractBasicBlock> elseEntry = Optional.empty();
        if (peekNextToken().getTag() == TokenTag.ELSE_TAG) {
            getNextToken();
            Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
            elseEntry = parseScope(
                    localVariableTableFactory.createChildLocalVariableTable(localVariableTable),
                    returnType, functionTable, className);
            Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        } else if (peekNextToken().getTag() == TokenTag.ELIF_TAG) {
            AbstractLocalVariableTable childTable = localVariableTableFactory.createChildLocalVariableTable(localVariableTable);
            elseEntry = Optional.of(parseBrStmt(childTable, returnType, functionTable, className, TokenTag.ELIF_TAG));
        }

        return new BasicBlock(new IfInstruction(cond, entry, elseEntry));
    }

    // todo
    private CondExpression parseCond(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable)
            throws ParseException
    {
//        CondExpression condExpression = parseCondExpr(localVariableTable, functionTable);
//        List<CondExpression> condExpressions = new ArrayList<>();
//        List<BoolOperation> boolOperations = new ArrayList<>();
//        while (ParseUtils.isBoolOperation(peekNextToken())) {
//            BoolOperation boolOperation = ParseUtils.getBoolOperation(getNextToken());
//            condExpressions.add(parseCondExpr(localVariableTable, functionTable));
//        }

        return parseCondExpr(localVariableTable, functionTable);
    }


    // todo
    private CondExpression parseCondExpr(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable)
            throws ParseException
    {
        Expression expression = parseExpression(localVariableTable, functionTable);
        List<Expression> expressions = new ArrayList<>();
        expressions.add(expression);
        List<ComparisonOperation> operations = new ArrayList<>();
        if (ParseUtils.isComparisonOperation(peekNextToken())) {
            operations.add(ParseUtils.getComparisonOperation(getNextToken()));
            expressions.add(parseExpression(localVariableTable, functionTable));
        }

        return new CondExpression(expressions, operations);
    }

    private AbstractBasicBlock parseDoStmt(
            AbstractLocalVariableTable localVariableTable,
            Optional<Type> returnType,
            IFunctionTable functionTable,
            String className) throws ParseException
    {
        Assert.assertTag(getNextToken(), TokenTag.DO_TAG);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        AbstractLocalVariableTable childTable = localVariableTableFactory.createChildLocalVariableTable(localVariableTable);
        Optional<AbstractBasicBlock> entry = parseScope(childTable, returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        Assert.assertTag(getNextToken(), TokenTag.WHILE_TAG);
        CondExpression cond = parseCond(localVariableTable, functionTable);
        return new BasicBlock(new DoInstruction(entry, cond));
    }

    private AbstractBasicBlock parseForStmt(
            AbstractLocalVariableTable localVariableTable,
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
        AbstractLocalVariableTable childTable = localVariableTableFactory.createChildLocalVariableTable(localVariableTable);
        Optional<AbstractBasicBlock> entry = parseScope(childTable, returnType, functionTable, className);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return new BasicBlock(new ForInstruction(varDecl, cond, action, entry));
    }

    private AbstractBasicBlock parseNew(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        return new BasicBlock(new SimpleInstruction(parseNewInt(localVariableTable, functionTable)));
    }

    private Expression parseNewInt(AbstractLocalVariableTable localVariableTable, IFunctionTable functionTable) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.NEW_TAG);
        Token token = getNextToken();
        Assert.assertValidDeclarationType(token, typeDictionary);
        String className = token.getValue();
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<Expression> arguments = parseFunArgs(localVariableTable, functionTable);
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ObjectCreationExpression(className, arguments);
    }
}
