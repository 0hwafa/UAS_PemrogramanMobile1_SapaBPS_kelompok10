plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("eclipse")
}

tasks.register("deleteLockedRJar") {
    doLast {
        val rJar = file("app/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar/debug/R.jar")
        if (rJar.exists()) {
            try {
                rJar.delete()
            } catch (_: Exception) {
                // ignore; best-effort delete
            }
        }
    }
}

gradle.projectsEvaluated {
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn("deleteLockedRJar")
    }
}