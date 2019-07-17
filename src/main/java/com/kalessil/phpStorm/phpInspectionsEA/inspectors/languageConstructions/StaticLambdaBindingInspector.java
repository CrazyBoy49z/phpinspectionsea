package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StaticLambdaBindingInspector extends PhpInspection {
    private static final String messageThis   = "'$this' can not be used in static closures.";
    private static final String messageParent = "Non-static method should not be used in static closures.";

    @NotNull
    public String getShortName() {
        return "StaticLambdaBindingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP540) && OpenapiTypesUtil.isLambda(function)) {
                    final boolean isTarget = OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwSTATIC);
                    if (isTarget) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                        if (body != null) {
                            for (final PsiElement element : PsiTreeUtil.findChildrenOfAnyType(body, Variable.class, MethodReference.class)) {
                                if (element instanceof Variable) {
                                    final Variable variable = (Variable) element;
                                    if (variable.getName().equals("this")) {
                                        holder.registerProblem(variable, messageThis, new TurnClosureIntoNonStaticFix(function.getFirstChild()));
                                        return;
                                    }
                                } else {
                                    final MethodReference reference = (MethodReference) element;
                                    final PsiElement base           = reference.getFirstChild();
                                    if (base instanceof ClassReference && base.getText().equals("parent")) {
                                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                        if (resolved instanceof Method && !((Method) resolved).isStatic()) {
                                            holder.registerProblem(reference, messageParent, new TurnClosureIntoNonStaticFix(function.getFirstChild()));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class TurnClosureIntoNonStaticFix implements LocalQuickFix {
        private static final String title = "Make the closure non-static";

        private final SmartPsiElementPointer<PsiElement> staticKeyword;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        TurnClosureIntoNonStaticFix(@NotNull PsiElement staticKeyword) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(staticKeyword.getProject());
            this.staticKeyword                = factory.createSmartPsiElementPointer(staticKeyword);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final PsiElement staticKeyword = this.staticKeyword.getElement();
                if (staticKeyword != null) {
                    staticKeyword.delete();
                }
            }
        }
    }
}