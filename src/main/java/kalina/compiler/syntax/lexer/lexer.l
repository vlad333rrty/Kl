%option noyywrap bison-bridge bison-locations

%{
#include <stdio.h>
#include <stdlib.h>
#include "TokenTag.h"
#include "logger/logger.h"

#define ERROR_STATE 255

struct Position{
    int line, pos, index;
};

typedef struct Position Position;

int getNumLen(int num) {
    int n = 0;
    do {
    	n++;
    	num /= 10;
    } while (num > 0);
    return n;
}

char* getPos(Position *p){
    char* buf = malloc(1 + getNumLen(p->line) + 2 + getNumLen(p->pos) + 1);
    sprintf(buf, "(%d, %d)", p->line, p->pos);
    return buf;
}

struct Fragment{
    Position start,follow;
};

typedef struct Fragment YYLTYPE;

union Token{
    char *ident;
    long num;
    char ch;
};

typedef union Token YYSTYPE;

void logFragment(YYLTYPE *type, char* value){
    char* message = "Unexpected token: ";
    char* start = getPos(&(type->start));
    char* follow = getPos(&(type->follow));
    char* buf = malloc(strlen(message) + strlen(value) + 1 + strlen(start) + 3 + strlen(follow));
    sprintf(buf, "%s%s %s - %s", message, value, start, follow);
    logError(buf);
    free(buf);
    free(start);
    free(follow);
}

int continued;
Position curPos;

#define YY_USER_ACTION {   \
        int i;\
        if (!continued) yylloc -> start = curPos;\
        continued=0;\
        for (i=0;i<yyleng;i++){\
            if (yytext[i]=='\n'){\
                curPos.line++;\
                curPos.pos=1;\
            }else{\
                curPos.pos++;\
            }\
            curPos.index++;\
        }\
        yylloc->follow=curPos;\
    }

void init(char *program){
    continued=0;
    curPos.line=1;
    curPos.pos=1;
    curPos.index=0;
    yy_scan_string(program);
}

%}

LETTER   [a-zA-Z]
DIGIT    [0-9]
IDENT    ({LETTER}|_)({LETTER}|{DIGIT}|_)*
NUMBER   {DIGIT}+
FLOAT_NUMBER {DIGIT}+(f|\.{DIGIT}*f)
DOUBLE_NUMBER {DIGIT}+(d|\.{DIGIT}*d?)
LONG_NUMBER {DIGIT}+(l|L)
STRING_LITERAL \"[^\"]*\"|'.*'
RETURN   ret
SHORT	short|int16
INT	int|int32
LONG	long|int64
ARRAY_TYPE {IDENT}(\[\])+

%%

[\n\t ]+
"//".*
=               {
                    yylval->ident=yytext;
                    return ASSIGN_TAG;
                }
\{              {
                    yylval->ident=yytext;
                    return LBRACE_TAG;
                }
\}              {
                    yylval->ident=yytext;
                    return RBRACE_TAG;
                }
\(              {
                    yylval->ident=yytext;
                    return LPAREN_TAG;
                }
\)              {
                    yylval->ident=yytext;
                    return RPAREN_TAG;
                }
,               {
                    yylval->ident=yytext;
                    return COMMA_TAG;
                }
;               {
                    yylval->ident=yytext;
                    return SEMICOLON_TAG;
                }
:               {
                    yylval->ident=yytext;
                    return COLON_TAG;
                }
->              {
                    yylval->ident=yytext;
                    return ARROW_TAG;
                }
\<-              {
                    yylval->ident=yytext;
                    return ARROW_LEFT_TAG;
                }
\+              {
                    yylval->ident=yytext;
                    return PLUS_TAG;
                }
-               {
                    yylval->ident=yytext;
                    return MINUS_TAG;
                }
\*              {
                    yylval->ident=yytext;
                    return MUL_TAG;
                }
[/]             {
                    yylval->ident=yytext;
                    return DIV_TAG;
                }
\<              {
                    yylval->ident=yytext;
                    return LESS_TAG;
                }
\>              {
                    yylval->ident=yytext;
                    return GREATER_TAG;
                }
\<=		{
                    yylval->ident=yytext;
                    return LEQ_TAG;
                }
\>=             {
                    yylval->ident=yytext;
                    return GEQ_TAG;
                }
==		{
                    yylval->ident=yytext;
                    return EQUAL_TAG;
		}
!=             {
                    yylval->ident=yytext;
                    return NOT_EQUAL_TAG;
                }
\.		{
                    yylval->ident=yytext;
                    return DOT_TAG;
		}
if              {
                    yylval->ident=yytext;
                    return IF_TAG;
                }
else            {
                    yylval->ident=yytext;
                    return ELSE_TAG;
                }
elif            {
                    yylval->ident=yytext;
                    return ELIF_TAG;
                }
for             {
                    yylval->ident=yytext;
                    return FOR_TAG;
                }
do              {
                    yylval->ident=yytext;
                    return DO_TAG;
                }
while           {
                    yylval->ident=yytext;
                    return WHILE_TAG;
                }
main            {
                    yylval->ident=yytext;
                    return MAIN_TAG;
                }
fun             {
                    yylval->ident=yytext;
                    return FUN_TAG;
                }
switch          {
                    yylval->ident=yytext;
                    return SWITCH_TAG;
                }
case            {
                    yylval->ident=yytext;
                    return CASE_TAG;
                }
break            {
                    yylval->ident=yytext;
                    return BREAK_TAG;
                }
default         {
                    yylval->ident=yytext;
                    return DEFAULT_TAG;
                }
new             {
                    yylval->ident=yytext;
                    return NEW_TAG;
                }
class           {
                    yylval->ident=yytext;
                    return CLASS_TAG;
                }
this            {
                    yylval->ident=yytext;
                    return THIS_TAG;
                }
final		{
                    yylval->ident=yytext;
                    return FINAL_TAG;
		}
&&		{
			yylval->ident = yytext;
			return BOOL_AND_TAG;
		}
\|\|		{
			yylval->ident = yytext;
			return BOOL_OR_TAG;
		}
\^		{
			yylval->ident = yytext;
			return XOR_TAG;
		}
{ARRAY_TYPE}          {
                         yylval->ident=yytext;
                         return ARRAY_TYPE_TAG;
                     }
{INT}                {
                       yylval->ident="int";
                       return INT_TAG;
                   }
{LONG}             {
                        yylval->ident="long";
                        return LONG_TAG;
                    }
float               {
                         yylval->ident=yytext;
                         return FLOAT_TAG;
                     }
double              {
                          yylval->ident=yytext;
                          return DOUBLE_TAG;
                      }
bool                {
                        yylval->ident=yytext;
                        return BOOL_TAG;
                    }
string              {
                           yylval->ident=yytext;
                           return STRING_TAG;
                    }
true|false          {
                        yylval->ident=yytext;
                        return BOOL_VALUE_TAG;
                    }
static		    {
                        yylval->ident=yytext;
                        return STATIC_TAG;
                    }
begin		    {
			yylval->ident=yytext;
                        return BEGIN_TAG;
		    }
extends		    {
			yylval->ident=yytext;
                        return EXTENDS_TAG;
		    }
const		    {
			yylval->ident=yytext;
                        return CONST_TAG;
		    }
private		    {
			yylval->ident=yytext;
                        return PRIVATE_TAG;
		    }
protected	    {
			yylval->ident=yytext;
                        return PROTECTED_TAG;
		    }

\?              {
                    yylval->ident=yytext;
                    return QUESTION_MARK_TAG;
                }
\[              {
                    yylval->ident=yytext;
                    return LEFT_SQ_BR_TAG;
                }
\]              {
                    yylval->ident=yytext;
                    return RIGHT_SQ_BR_TAG;
                }
{RETURN}	{
                    yylval->ident=yytext;
                    return RETURN_TAG;
                }
{IDENT}			{
				yylval->ident=yytext;
				return IDENT_TAG;
			}

{NUMBER}		{
                   		 yylval->ident=yytext;
				return NUMBER_TAG;
			}
{LONG_NUMBER}		{
                   		 yylval->ident=yytext;
				return LONG_NUMBER_TAG;
			}
{FLOAT_NUMBER}		{
                   		 yylval->ident=yytext;
				return FLOAT_NUMBER_TAG;
			}
{DOUBLE_NUMBER}		{
                   		 yylval->ident=yytext;
				return DOUBLE_NUMBER_TAG;
			}
{STRING_LITERAL}	{
                   		 yylval->ident=yytext;
				return STRING_LITERAL_TAG;
			}
.			{
				yylval->ident = yytext;
				return ERROR_STATE;
                        }
%%

#define SIZE 1<<28

int main(int argc, char **argv){
    logInfo("Starting lexer\n");
    if (argc!=3) {
        logError("Input/output file names should be provided! Stopping lexer\n");
        return ERROR_STATE;
    }
    FILE *file=fopen(argv[1],"r");
    if (!file) {
        logError("Can not open the source file\n");
        return ERROR_STATE;
    }
    FILE* out = fopen(argv[2],"w");
    if (!out) {
        logError("Can not open the dest file\n");
        return ERROR_STATE;
    }
    char *buf = malloc(SIZE);
    char c;
    int i=0;
    while ((c=fgetc(file))!=EOF && i < SIZE) {
        buf[i++]=c;
    }
    if (i == SIZE) {
        logError("File is too long!\n");
        return ERROR_STATE;
    }
    buf[i]='\0';
    if (ferror(file)) {
        logError("An error occurred while reading a file\n");
        return ERROR_STATE;
    }
    fclose(file);

    int tag;
    YYSTYPE value;
    YYLTYPE coords;

    init(buf);

    int error = 0;
    do {
    	tag = yylex(&value,&coords);
    	if (tag == ERROR_STATE) {
    		logFragment(&coords, value.ident);
    		error = 1;
    	}
        if (tag > 0){
        	fprintf(out,"%d %s\n", tag - 1, value.ident);
        }
    } while(tag != 0);

    fclose(out);
    free(buf);
    if (error == 1) {
    	return ERROR_STATE;
    }
    logInfo("No errors. Stopping lexer\n");

    return 0;
}