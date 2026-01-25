package com.sagrd.codereviewerapp.data

import dev.snipme.highlights.model.SyntaxLanguage

// Project Type Enum for dynamic syntax highlighting
enum class ProjectType(val syntaxLanguage: SyntaxLanguage, val displayName: String) {
    KOTLIN(SyntaxLanguage.KOTLIN, "Kotlin"),
    BLAZOR(SyntaxLanguage.CSHARP, "Blazor (C#)")
}