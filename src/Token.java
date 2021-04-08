public class Token {
    String tokenType;
    String lexeme;
    int position;
    boolean valid;


    public String toString(){
        return "["+tokenType + ", "+ lexeme + ", " + position + ", "+ valid +"]";
    }
}




/**
 * [eq, ==, 1] [plus, +, 1] [or, |, 1] [openpar, (, 1] [semi, ;, 11] [if, if, 1] [public, public, 1] [read, read, 1]
 * [noteq, <>, 2] [minus, -, 2] [and, &, 2] [closepar, ), 2]	[comma, ,, 2] [then, then, 2] [private, private, 2] [write, write, 2]
 * [lt, <, 3] [mult, *, 3] [not, !, 3] [opencubr, {, 3] [dot, ., 3] [else, else, 3] [func, func, 3] [return, return, 3]
 * [gt, >, 4]	[div, /, 4] [qmark, ?, 4] [closecubr, }, 4] [colon, :, 4] [integer, integer, 4]	[var, var, 4] [main, main, 4]
 * [leq, <=, 5] [assign, =, 5]	[opensqbr, [, 5] [coloncolon, ::, 5] [float, float, 5]	[class, class, 5] [inherits, inherits, 5]
 * [geq, >=, 6] [closesqbr, ], 6] [string, string, 6] [while, while, 7] [break, break, 7]
 * [void, void, 7]	[continue, continue, 7]
 *
 * [intnum, 0, 13]
 * [floatnum, 1.23, 20]
 * [id, abc, 25]
 * [blockcmt, /* this is a single line block comment * /, 31]
 * [inlinecmt, // this is an inline comment, 38]
 * [stringlit, this is a string literal, 40]
 */

