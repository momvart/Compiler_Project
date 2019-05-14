package sut.momtsaber.clikecompiler.utils;

import com.google.gson.*;

import java.lang.reflect.Type;

import sut.momtsaber.clikecompiler.cfg.*;

public class CFGSymbolDeserializer implements JsonDeserializer<CFGSymbol>
{

    @Override
    public CFGSymbol deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.getAsJsonObject().has("id"))
            return context.deserialize(json, CFGNonTerminal.class);
        else
            return context.deserialize(json, CFGTerminal.class);
    }
}
