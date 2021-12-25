%option noyywrap bison-bridge bison-locations

%{
#include <stdio.h>
#include <stdlib.h>
#include "TokenTag.h"
#include "logger/logger.h"

#define ERROR_STATE -2

struct Position{
    int line, pos, index;
};

typedef struct Position Position;

void printPos(Position *p){
    printf("(%d, %d)",p->line, p->pos);
}

struct Fragment{
    Position start,follow;
};

typedef struct Fragment YYLTYPE;

void printFragment(YYLTYPE *type){
    printPos(&(type->start));
    printf(" - ");
    printPos(&(type->follow));
}

union Token{
    char *ident;
    long num;
    char ch;
};

typedef union Token YYSTYPE;

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

int state = 0;

%}

LETTER   [a-zA-Z]
DIGIT    [0-9]
IDENT    {LETTER}({LETTER}|{DIGIT})*
NUMBER   {DIGIT}+
FLOAT_NUMBER {DIGIT}+(f|\.{DIGIT}*f)
DOUBLE_NUMBER {DIGIT}+(d|\.{DIGIT}*d?)
LONG_NUMBER {DIGIT}+(l|L)
STRING_LITERAL \".*\"|'.*'
RETURN   ret

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
\<=
                {
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
char            {
                        yylval->ident=yytext;
                        return CHAR_TAG;
                    }
short               {
                         yylval->ident=yytext;
                         return SHORT_TAG;
                     }
int                 {
                       yylval->ident=yytext;
                       return INT_TAG;
                   }
long                {
                        yylval->ident=yytext;
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
true                {
                        yylval->ident=yytext;
                        return TRUE_TAG;
                    }
false               {
                        yylval->ident=yytext;
                        return FALSE_TAG;
                    }
static		    {
                        yylval->ident=yytext;
                        return STATIC_TAG;
                    }
begin		    {
			yylval->ident=yytext;
                        return BEGIN_TAG;
		    }
\`               {
                    yylval->ident=yytext;
                    return QUOTE_TAG;
                }
\"               {
                    yylval->ident=yytext;
                    return DOUBLE_QUOTE_TAG;
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
				char *message = "Unexpected token: ";
				char *buf = (char*)malloc(strlen(message) + strlen(yytext));
				sprintf(buf, "%s %s", message, yytext);
				logError(buf);
				state = ERROR_STATE;
				return 0;
                        }
%%

#define SIZE 1<<10

int main(int argc, char **argv){
    logInfo("Starting lexer\n");
    if (argc!=3) {
        logError("Input/output file names should be provided! Stopping lexer\n");
        return 0;
    }
    FILE *file=fopen(argv[1],"r");
    if (!file) {
        logError("Can not open the source file\n");
        return 0;
    }
    FILE* out = fopen(argv[2],"w");
    if (!out) {
        logError("Can not open the dest file\n");
        return 0;
    }
    char buf[SIZE];
    char c;
    int i=0;
    while ((c=fgetc(file))!=EOF && i < SIZE) {
        buf[i++]=c;
    }
    if (i == SIZE) {
        logError("File is too long!\n");
        return 1;
    }
    buf[i]='\0';
    if (ferror(file)) {
        logError("An error occurred while reading a file\n");
        return 0;
    }
    fclose(file);

    int tag;
    YYSTYPE value;
    YYLTYPE coords;

    init(buf);

    do {
    	tag = yylex(&value,&coords);
        if (tag > 0){
        	fprintf(out,"%d %s\n", tag - 1, value.ident);
        }
    } while(tag != 0);

     if (state == ERROR_STATE) {
     	fclose(out);
     	out = fopen(argv[2],"w");
     	fclose(out);
     	return 0;
     }

    fclose(out);
    logInfo("No errors. Stopping lexer\n");

    return 0;
}