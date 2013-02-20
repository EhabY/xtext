/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.typesystem.conformance;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.xbase.typesystem.conformance.TypeConformanceComputationArgument.Internal;
import org.eclipse.xtext.xbase.typesystem.references.AnyTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.CompoundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.LightweightBoundTypeArgument;
import org.eclipse.xtext.xbase.typesystem.references.LightweightMergedBoundTypeArgument;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.UnboundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.WildcardTypeReference;
import org.eclipse.xtext.xbase.typesystem.util.BoundTypeArgumentMerger;
import org.eclipse.xtext.xbase.typesystem.util.BoundTypeArgumentSource;
import org.eclipse.xtext.xbase.typesystem.util.VarianceInfo;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@NonNullByDefault
class UnboundConformanceStrategy extends TypeConformanceStrategy<UnboundTypeReference> {
	protected UnboundConformanceStrategy(TypeConformanceComputer conformanceComputer) {
		super(conformanceComputer);
	}

	@Override
	protected TypeConformanceResult doVisitTypeReference(UnboundTypeReference left, LightweightTypeReference right, TypeConformanceComputationArgument.Internal<UnboundTypeReference> param) {
		TypeConformanceResult result = tryResolveAndCheckConformance(left, right, param);
		if (result != null)
			return result;
		return TypeConformanceResult.create(param, ConformanceHint.INCOMPATIBLE);
	}
	
	@Override
	protected TypeConformanceResult doVisitAnyTypeReference(UnboundTypeReference left, AnyTypeReference right,
			Internal<UnboundTypeReference> param) {
		return TypeConformanceResult.create(param, ConformanceHint.SUCCESS);
	}

	@Nullable
	protected TypeConformanceResult tryResolveAndCheckConformance(UnboundTypeReference left, LightweightTypeReference right,
			TypeConformanceComputationArgument.Internal<UnboundTypeReference> param) {
		List<LightweightBoundTypeArgument> hints = left.getAllHints();
		if (hints.isEmpty() && !param.unboundComputationAddsHints) {
			return TypeConformanceResult.create(param, ConformanceHint.INCOMPATIBLE); 
		}
		if (!left.isConformantToConstraints(right)) {
			return TypeConformanceResult.create(param, ConformanceHint.INCOMPATIBLE);
		}
		List<LightweightBoundTypeArgument> hintsToProcess = Lists.newArrayListWithCapacity(hints.size());
		List<LightweightBoundTypeArgument> inferredHintsToProcess = Lists.newArrayListWithCapacity(hints.size());
		int laterCount = 0;
		boolean inferredAsWildcard = false;
		for(LightweightBoundTypeArgument hint: hints) {
			if (hint.getDeclaredVariance() != null) {
				hintsToProcess.add(hint);
				if (hint.getSource() == BoundTypeArgumentSource.INFERRED) {
					if (hint.getTypeReference() instanceof WildcardTypeReference) {
						inferredAsWildcard = true;
					}
					inferredHintsToProcess.add(hint);
				} else if (hint.getSource() == BoundTypeArgumentSource.INFERRED_LATER) {
					laterCount++;
				}
			}
		}
		if (hintsToProcess.isEmpty() && param.unboundComputationAddsHints) {
			if (right instanceof WildcardTypeReference) {
				List<LightweightTypeReference> bounds = ((WildcardTypeReference) right).getUpperBounds();
				for(LightweightTypeReference upperBound: bounds)
					left.acceptHint(upperBound, BoundTypeArgumentSource.INFERRED, this, VarianceInfo.OUT, VarianceInfo.OUT);
			} else {
				left.acceptHint(right, BoundTypeArgumentSource.INFERRED, this, VarianceInfo.OUT, VarianceInfo.OUT);
			}
			return TypeConformanceResult.create(param, ConformanceHint.SUCCESS);
		} else {
			BoundTypeArgumentMerger merger = left.getOwner().getServices().getBoundTypeArgumentMerger();
			LightweightMergedBoundTypeArgument mergeResult = merger.merge(inferredHintsToProcess.isEmpty() || (laterCount > 1 && inferredAsWildcard) ? hintsToProcess : inferredHintsToProcess, left.getOwner());
			if (mergeResult != null && mergeResult.getVariance() != null) {
				TypeConformanceComputationArgument newParam = param;
				LightweightTypeReference mergeResultReference = mergeResult.getTypeReference();
				if (right.isWildcard() && mergeResultReference.isWildcard()) {
					if (right.getLowerBoundSubstitute().isAny()) {
						LightweightTypeReference lowerBoundMergeResult = mergeResultReference.getLowerBoundSubstitute();
						if (!lowerBoundMergeResult.isAny()) {
							mergeResultReference = lowerBoundMergeResult;
						}
					} else {
						newParam = TypeConformanceComputationArgument.Internal.create(param.reference, param.rawType, param.asTypeArgument || mergeResult.getTypeReference().isWildcard(), 
								param.allowPrimitiveConversion, param.allowPrimitiveWidening, param.unboundComputationAddsHints, param.allowSynonyms);
					}
				} else if (mergeResultReference.isWildcard()) {
					newParam = TypeConformanceComputationArgument.Internal.create(param.reference, param.rawType, param.asTypeArgument || mergeResult.getTypeReference().isWildcard(), 
							param.allowPrimitiveConversion, param.allowPrimitiveWidening, param.unboundComputationAddsHints, param.allowSynonyms);
				}
				TypeConformanceResult result = conformanceComputer.isConformant(mergeResultReference, right, newParam);
				return result;
			}
		}
		return TypeConformanceResult.create(param, ConformanceHint.INCOMPATIBLE);
	}
	
	@Override
	protected TypeConformanceResult doVisitMultiTypeReference(UnboundTypeReference left, CompoundTypeReference right, TypeConformanceComputationArgument.Internal<UnboundTypeReference> param) {
		return doVisitTypeReference(left, right, param);
	}
	
	@Override
	protected TypeConformanceResult doVisitUnboundTypeReference(UnboundTypeReference left, UnboundTypeReference right,
			TypeConformanceComputationArgument.Internal<UnboundTypeReference> param) {
		if (left.getHandle().equals(right.getHandle())) {
			return TypeConformanceResult.create(param, ConformanceHint.SUCCESS);
		}
		if (param.unboundComputationAddsHints && (!left.hasSignificantHints() || !right.hasSignificantHints())) {
			left.acceptHint(right, BoundTypeArgumentSource.INFERRED, this, VarianceInfo.OUT, VarianceInfo.OUT);
			return TypeConformanceResult.create(param, ConformanceHint.SUCCESS);
		}
		if (left.getAllHints().equals(right.getAllHints()))
			return TypeConformanceResult.create(param, ConformanceHint.SUCCESS);
		TypeConformanceResult result = tryResolveAndCheckConformance(left, right, param);
		if (result != null)
			return result;
		return TypeConformanceResult.create(param, ConformanceHint.INCOMPATIBLE);
	}
}