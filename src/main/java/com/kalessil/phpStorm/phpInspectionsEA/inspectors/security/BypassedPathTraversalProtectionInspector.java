package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BypassedPathTraversalProtectionInspector extends LocalInspectionTool {
    private static final String messageFilterVar = "The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.";

    @NotNull
    public String getShortName() {
        return "BypassedPathTraversalProtectionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("str_replace")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 3) {
                        final Set<String> second = ExpressionSemanticUtil.resolveAsString(arguments[1]);
                        if (!second.isEmpty() && second.contains("")) {
                            /* collect variants */
                            final PsiElement searchPatterns = arguments[0];
                            final List<PsiElement> variants = new ArrayList<>();
                            if (searchPatterns instanceof ArrayCreationExpression) {
                                for (final PsiElement child : searchPatterns.getChildren()) {
                                    if (!(child instanceof ArrayHashElement)) {
                                        variants.add(child.getFirstChild());
                                    }
                                }
                            } else {
                                variants.add(searchPatterns);
                            }
                            /* check variants */
                            final boolean withQuickFix = !(searchPatterns instanceof ArrayCreationExpression);
                            for (final PsiElement variant : variants) {
                                final Set<String> first = ExpressionSemanticUtil.resolveAsString(variant);
                                if (!first.isEmpty() && (first.contains("../") || first.contains("..\\"))) {
                                    final String replacement = String.format(
                                            "preg_replace('/\\.+[\\/\\\\]+/', '', %s)",
                                            arguments[2].getText()
                                    );
                                    holder.registerProblem(
                                            reference,
                                            messageFilterVar,
                                            withQuickFix ? new UsePregReplaceFix(replacement) : null
                                    );
                                }
                                first.clear();
                            }
                            variants.clear();
                        }
                        second.clear();
                    }
                }
            }
        };
    }

    private static final class UsePregReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Harden with preg_replace(...)";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UsePregReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
