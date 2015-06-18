package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexProjectComponent;

/**
 * @author Stanislav Turza
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {

    public CompletionContributor() {
        // $app['<caret>']
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new ArrayAccessCompletionProvider());
        // $app[''] = $app->extend('<caret>', ...
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new ExtendsMethodParameterListCompletionProvider());
    }

    private static class ArrayAccessCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement element = parameters.getPosition().getParent();

            if(!SilexProjectComponent.isEnabled(element.getProject())) {
                return;
            }

            if (!(element instanceof StringLiteralExpression)) {
                return;
            }

            Container container = Utils.getContainerFromArrayAccessLiteral((StringLiteralExpression) element);
            if (container == null) {
                return;
            }

            for (Service service : container.getServices().values()) {
                resultSet.addElement(new ServiceLookupElement(service));
            }

            for (Parameter parameter : container.getParameters().values()) {
                resultSet.addElement(new ParameterLookupElement(parameter));
            }

            for (String key : container.getContainers().keySet()) {
                resultSet.addElement(new ContainerLookupElement(key));
            }
        }
    }

    private static class ExtendsMethodParameterListCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement element = parameters.getPosition().getParent();

            if(!SilexProjectComponent.isEnabled(element.getProject())) {
                return;
            }

            if (!(element instanceof StringLiteralExpression)) {
                return;
            }

            if (!Utils.isFirstParameterOfPimpleContainerMethod((StringLiteralExpression) element)) {
                return;
            }

            for (Service service : ContainerResolver.get(element.getProject()).getServices().values()) {
                resultSet.addElement(new ServiceLookupElement(service));
            }

            for (Parameter parameter : ContainerResolver.get(element.getProject()).getParameters().values()) {
                resultSet.addElement(new ParameterLookupElement(parameter));
            }
        }
    }
}
