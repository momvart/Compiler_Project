package sut.momtsaber.clikecompiler.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import sut.momtsaber.clikecompiler.cfg.CFGAction;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;

public class CFGSymbolDeserializer implements JsonDeserializer<CFGSymbol>
{

    @Override
    public CFGSymbol deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.getAsJsonObject().has("id"))
            return context.deserialize(json, CFGNonTerminal.class);
        else if (json.getAsJsonObject().has("name"))
            return context.deserialize(json, CFGAction.class);
        else
            return context.deserialize(json, CFGTerminal.class);
    }
}
