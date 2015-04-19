grammar Ff;
start: s* EOF;
b: '{' s* '}';
ifElse: 'if' e b 'else' s;
if_: 'if' e b;
while_: 'while' e b;
h: b #blockHybrid
 | ifElse #ifElseHybrid
 | if_ #ifHybrid
 | while_ #whileHybrid
 ;
s: h #hStmt
 | e ';' #exprStmt
 | ';' #noOp
 ;
e: STR+ #str
 | NAME #name
 | '['(e(','e)*)?']' #list
 | '{'(e':'e(e':'e)*)?'}' #dict
 | e'.'NAME #attr
 | e'['e']' #getItem
 | e'('(e(','e)*)?')' #call
 | '('e')' #par
 | e op=('=='|'+') e #binOp
 | NAME ':=' e #decl
 | NAME '=' e #assign
 | e'.'NAME '=' e #attrAssign
 | e'['e']' '=' e #setItem
 | '\\' NAME* b #lambda
 | h #hExpr
 ;
STR: [0-9]+ ('.' [0-9]*)?
   | '.' [0-9]+
   |     ["] (~["] | '\\' ["]) * ["]
   |     ['] (~['] | '\\' [']) * [']
   | 'r' ["] (~["] | '\\' ["]) * ["]
   | 'r' ['] (~['] | '\\' [']) * [']
   ;
NAME: [a-zA-Z0-9_$]+;
CMT: '#' ~'\n'* -> skip;
WS: [ \t\r\n]+ -> skip;
