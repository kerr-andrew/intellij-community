// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.gradle.service.resolve;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiWildcardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.typeEnhancers.ClosureAsAnonymousParameterEnhancer;

/**
 * @author Vladislav.Soroka
 */
public class GradleClosureAsAnonymousParameterEnhancer extends ClosureAsAnonymousParameterEnhancer {
  @Nullable
  @Override
  protected PsiType getClosureParameterType(@NotNull GrClosableBlock closure, int index) {

    PsiFile file = closure.getContainingFile();
    if (file == null || !FileUtilRt.extensionEquals(file.getName(), GradleConstants.EXTENSION)) return null;

    PsiType psiType = super.getClosureParameterType(closure, index);
    if (psiType instanceof PsiWildcardType) {
      PsiWildcardType wildcardType = (PsiWildcardType)psiType;
      if (wildcardType.isSuper() && wildcardType.getBound() != null &&
          wildcardType.getBound().equalsToText(GradleCommonClassNames.GRADLE_API_SOURCE_SET)) {
        return wildcardType.getBound();
      }
      if (wildcardType.isSuper() && wildcardType.getBound() != null &&
          wildcardType.getBound().equalsToText(GradleCommonClassNames.GRADLE_API_DISTRIBUTION)) {
        return wildcardType.getBound();
      }
    }

    return null;
  }
}