lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Deca lexer rules.

COMMENT : '/*' .*? '*/' { skip(); };

COMMENT_MONO : '//' (~('\r' | '\n'))* { skip(); };

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) { skip(); };
        
fragment LETTER : 'a'  ..  'z' | 'A'  ..  'Z';
fragment DIGIT : '0'  ..  '9';
ASM : 'asm';
CLASS : 'class';
EXTENDS : 'extends';
ELSE : 'else';
FALSE : 'false';
IF : 'if';
INSTANCEOF : 'instanceof';
NEW : 'new';
NULL : 'null';
READINT : 'readInt';
READFLOAT : 'readFloat';
PRINT : 'print';
PRINTLN : 'println';
PRINTLNX : 'printlnx';
PRINTX : 'printx';
PROTECTED : 'protected';
RETURN : 'return';
THIS : 'this';
TRUE : 'true';
WHILE : 'while';


IDENT : (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')*;

LT : '<' ; 
GT : '>' ; 
EQUALS : '=' ;
PLUS : '+' ; 
MINUS : '-' ;
TIMES : '*' ; 
SLASH : '/' ;
PERCENT : '%' ;
DOT : '.' ;
COMMA : ',' ;
OPARENT : '(' ;
CPARENT : ')' ;
OBRACE : '{' ;
CBRACE : '}' ;
EXCLAM  : '!' ;
SEMI : ';' ;
EQEQ : '==' ; 
NEQ : '!=' ; 
GEQ : '>=' ; 
LEQ : '<=' ; 
AND : '&&' ; 
OR : '||' ;


fragment POSITIVE_DIGIT : '1'  ..  '9';
INT : '0' | POSITIVE_DIGIT DIGIT*;//todo Une erreur de compilation est levée si un littéral entier n’est pas codable comme un entier signé positif sur 32 bits.


fragment NUM : DIGIT+;
fragment SIGN : [+-]?;
fragment EXP : ('E' | 'e') SIGN NUM;
fragment DEC : NUM '.' NUM;
fragment FLOATDEC : DEC EXP? [Ff]?;
fragment DIGITHEX : '0'  ..  '9' | 'A'  ..  'F' | 'a'  ..  'f';
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX : ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN NUM [Ff]?;
FLOAT : (FLOATDEC | FLOATHEX) {  Float temp = Float.parseFloat(getText());
                                 if(Float.isInfinite(temp) || Float.isNaN(temp)){
                                    throw new InvalidFloat(this,getInputStream());
                                 }}
      | (FLOATDEC | FLOATHEX) [A-Za-z] { 
                                 LexerNoViableAltException e = new LexerNoViableAltException(
                                    this, _input, _tokenStartCharIndex, null);
                                 notifyListeners(e); };



fragment STRING_CAR : '\\' ('"' | '\\' | 'n' | 'r' | 't') ;
STRING : '"' (~["\\\n] | STRING_CAR)* '"' ;
MULTI_LINE_STRING : '"' (~["\\] | '\n' | STRING_CAR)* '"' ;

fragment FILENAME : (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE : '#include' (' ')* '"' FILENAME '"' {
   doInclude(getText());
   skip();}
   ;
DEFAULT : . { LexerNoViableAltException e =
            new LexerNoViableAltException(
                this,
                _input,
                _tokenStartCharIndex,
                null 
            );
        notifyListeners(e); } ; 