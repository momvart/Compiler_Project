package sut.momtsaber.clikecompiler.cfg;

public class CFGAction extends CFGSymbol
{
    private String name;

    public String getName()
    {
        return name;
    }

    public static class Names
    {
        public static final String PUSH_TOKEN = "pt";

        public static final String START_PROGRAM = "pstart";
        public static final String END_PROGRAM = "pend";

        public static final String BEGIN_SCOPE = "begin_scope";
        public static final String END_SCOPE = "end_scope";

        public static final String DECLARE_VAR = "var_dec";
        public static final String DECLARE_ARRAY = "arr_dec";

        public static final String DECLARE_FUNC = "func_dec";
        public static final String DECLARE_PARAM_VAR = "param_var_dec";
        public static final String DECLARE_PARAM_ARRAY = "param_arr_dec";
        public static final String FUNC_INIT = "func_init";
        public static final String PUSH_RETURN_VAL = "push_retval";
        public static final String PUSH_VOID = "push_void";
        public static final String RETURN = "return";

        public static final String CONTINUE = "continue";
        public static final String BREAK = "break";

        public static final String MULTIPLY = "mult";
        public static final String ADD_SUBTRACT = "add_sub";
        public static final String APPLY_SIGN = "apply_sign";

        public static final String LABEL = "label";
        public static final String SAVE = "save";
        public static final String WHILE = "while";

        public static final String JPF_SAVE = "jpf_save";
        public static final String JP = "jp";

        public static final String PUT_1 = "put_1";
        public static final String JPF_CMP_SAVE = "jmp_cmp_save";
        public static final String JPF = "jpf";

    }
}
