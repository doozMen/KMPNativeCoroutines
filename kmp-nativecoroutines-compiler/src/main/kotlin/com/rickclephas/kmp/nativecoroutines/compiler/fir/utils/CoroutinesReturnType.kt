package com.rickclephas.kmp.nativecoroutines.compiler.fir.utils

import com.rickclephas.kmp.nativecoroutines.compiler.utils.ClassIds
import com.rickclephas.kmp.nativecoroutines.compiler.utils.CoroutinesReturnType
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.toClassLikeSymbol
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.fullyExpandedClass
import org.jetbrains.kotlin.fir.resolve.isSubclassOf
import org.jetbrains.kotlin.fir.types.toLookupTag

internal fun FirCallableDeclaration.getCoroutinesReturnType(session: FirSession): CoroutinesReturnType? {
    val symbol = returnTypeRef.toClassLikeSymbol(session)?.fullyExpandedClass(session) ?: return null
    return coroutinesReturnTypes.firstNotNullOfOrNull { (lookupTag, returnType) ->
        returnType.takeIf { symbol.isSubclassOf(lookupTag, session, isStrict = false, lookupInterfaces = true) }
    }
}

private val coroutinesReturnTypes = mapOf(
    ClassIds.stateFlow.toLookupTag() to CoroutinesReturnType.Flow.State,
    ClassIds.flow.toLookupTag() to CoroutinesReturnType.Flow.Generic,
    ClassIds.coroutineScope.toLookupTag() to CoroutinesReturnType.CoroutineScope,
)
