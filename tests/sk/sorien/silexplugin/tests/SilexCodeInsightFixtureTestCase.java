package sk.sorien.silexplugin.tests;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.pimple.PimplePhpTypeProvider;

import java.util.Arrays;
import java.util.List;

/**
 * @author Stanislav Turza
 */
abstract public class SilexCodeInsightFixtureTestCase extends LightCodeInsightFixtureTestCase {

    public void assertCompletionContains(LanguageFileType languageFileType, String configureByText, String... lookupStrings) {

        myFixture.configureByText(languageFileType, configureByText);
        myFixture.completeBasic();

        completionContainsAssert(lookupStrings);
    }

    public void assertCompletionEquals(LanguageFileType languageFileType, String configureByText, String... lookupStrings) {

        myFixture.configureByText(languageFileType, configureByText);
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        if (lookupElements == null) {
            return;
        }

        if (lookupElements.size() != lookupStrings.length) {
            fail(String.format("completion %s have to contain only %s", lookupElements.toString(), Arrays.toString(lookupStrings)));
        }

        completionContainsAssert(lookupStrings);
    }

    private void completionContainsAssert(String[] lookupStrings) {
        List<String> lookupElements = myFixture.getLookupElementStrings();
        for (String s : Arrays.asList(lookupStrings)) {
            if(lookupElements != null && !lookupElements.contains(s)) {
                fail(String.format("completion %s does not contains \"%s\"", lookupElements.toString(), s));
            }
        }
    }

    public void assertCompletionResultEquals(LanguageFileType languageFileType, String configureByText, String result) {
        myFixture.configureByText(languageFileType, configureByText);
        myFixture.completeBasic();
        myFixture.type("\n");
        myFixture.checkResult(result);
    }

    public void assertTypeSignatureEquals(LanguageFileType languageFileType, @NotNull Class aClass, String configureByText, String typeSignature) {
        myFixture.configureByText(languageFileType, configureByText);
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        psiElement = PsiTreeUtil.getParentOfType(psiElement, aClass);

        PhpTypeProvider2[] typeAnalyser = Extensions.getExtensions(PhpTypeProvider2.EP_NAME);

        for (PhpTypeProvider2 provider : typeAnalyser) {

            if (provider instanceof PimplePhpTypeProvider) {

                String providerType = provider.getType(psiElement);
                if (providerType != null) {
                    providerType = "#" + provider.getKey() + providerType;
                }

                assertEquals(typeSignature, providerType);
            }
        }
    }

    public void assertPhpReferenceSignatureEquals(LanguageFileType languageFileType, @NotNull Class aClass, String configureByText, String typeSignature) {
        myFixture.configureByText(languageFileType, configureByText);
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        psiElement = PsiTreeUtil.getParentOfType(psiElement, aClass);

        if (!(psiElement instanceof PhpReference)) {
            fail("Element is not PhpReference.");
        }

        assertEquals(typeSignature, ((PhpReference)psiElement).getSignature());
    }

    public void assertSignatureEquals(String typeSignature, String phpClassType) {

        PhpTypeProvider2[] typeAnalyser = Extensions.getExtensions(PhpTypeProvider2.EP_NAME);

        for (PhpTypeProvider2 provider : typeAnalyser) {

            if (provider instanceof PimplePhpTypeProvider) {
                assertEquals(provider.getBySignature(typeSignature, myFixture.getProject()).iterator().next().getType().toString(), phpClassType);
            }
        }
    }

}
