package sut.momtsaber.clikecompiler.cfg;

import java.util.Objects;

public class CFGAction extends CFGSymbol
{
    private final String name;

    public CFGAction(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CFGAction)) return false;
        return Objects.equals(name, ((CFGAction)o).name);
    }

    @Override
    public String toString()
    {
        return '#' + name;
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
        public static final String SET_RETURN_VAL = "set_retval";
        public static final String RETURN = "return";
        @Deprecated
        public static final String FIND_FUNC = "find_func";
        public static final String START_ARGS = "start_args";
        @Deprecated
        public static final String PUT_ARG = "put_arg";
        public static final String CALL = "call";

        public static final String CONTINUE = "continue";
        public static final String BREAK = "break";

        public static final String PUT_VARIABLE = "put_var";
        public static final String PUT_ARRAY_ELEMENT = "put_arr_elem";
        public static final String PUT_NUMBER = "put_num";
        public static final String POP_VALUE = "pop_value";
        public static final String ASSIGN = "assign";

        public static final String MULTIPLY = "mult";
        public static final String ADD_SUBTRACT = "add_sub";
        public static final String APPLY_SIGN = "apply_sign";
        public static final String COMPARE = "compare";

        public static final String LABEL = "label";
        public static final String SAVE = "save";
        public static final String WHILE = "while";

        public static final String JPF_SAVE = "jpf_save";
        public static final String JP = "jp";

        public static final String PUT_1 = "put_1";
        public static final String JPF_CMP_SAVE = "jpf_cmp_save";
        public static final String JPF = "jpf";

        public static final String BEGIN_WHILE_SCOPE = "begin_while_scope";
        public static final String END_WHILE_SCOPE = "end_while_scope";

        public static final String BEGIN_SWICH_SCOPE = "begin_switch_scope";
        public static final String END_SWITCH_SCOPE = "end_switch_scope";
    }
}
