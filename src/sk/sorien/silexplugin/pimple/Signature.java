package sk.sorien.silexplugin.pimple;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Stanislav Turza
 */
public class Signature {

    private String classSignature = "";
    private final ArrayList<String> parameters = new ArrayList<String>();

    public Signature(@Nullable String expression) {
        set(expression);
    }

    public Signature() {
    }

    public void set(@Nullable String expression) {

        classSignature = "";
        parameters.clear();

        if (expression != null) {
            int openBraceletIndex = expression.indexOf('[');
            int closeBraceletIndex = expression.lastIndexOf(']');

            if ((openBraceletIndex == -1) || (closeBraceletIndex == -1)) {
                classSignature = expression;
                return;
            }

            classSignature = expression.substring(0, openBraceletIndex);

            String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(expression.substring(openBraceletIndex + 1, closeBraceletIndex), "][");
            Collections.addAll(parameters, split);
        }
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public String getClassSignature() {
        return classSignature;
    }

    public Boolean hasParameters() {
        return !parameters.isEmpty();
    }

    @Override
    public String toString() {
        String string = classSignature;

        for (String param : parameters) {
            string = string + "[" + param + "]";
        }

        return string;
    }
}
