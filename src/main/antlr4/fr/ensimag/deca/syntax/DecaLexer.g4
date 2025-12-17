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
                // A FAIRE : Règle bidon qui reconnait tous les caractères.
                // A FAIRE : Il faut la supprimer et la remplacer par les vraies règles.

fragment EOL :'\n';
fragment LETTER : 'a'  ..  'z' | 'A'  ..  'Z';
fragment DIGIT : '0'  ..  '9';
IDENT : (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')*;//todo Exception : les mots réservés ne sont pas des identificateurs.


PLUS : '+' ;

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
FLOAT : FLOATDEC | FLOATHEX;

fragment STRING_CAR : ~('"' | '\\' | '\n');
STRING : '"' (STRING_CAR | '\\"' | '\\\\')* '"';
MULTI_LINE_STRING : '"' (STRING_CAR | '\n' | '\\"' | '\\\\')* '"';


COMMENT : '/*' .*? '*/' { skip(); };
UNCLOSED_COMMENT
    : '/*' .*? EOF
      { System.err.println(getSourceName() + ":" +
                           getLine() + ":" +
                           getCharPositionInLine() + 
                           ": commentaire non fermé"); } // todo: gestion corecte des erreur
    ;
COMMENT_MONO : '//' (~('\r' | '\n'))* { skip(); };

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {
              skip(); 
          }
    ;
fragment FILENAME : (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE : '#include' (' ')* '"' FILENAME '"' {
   doInclude(getText());
   skip();}
   ;