plugins {
    java
    id("fr.hadaly.gradle.openapidiff")
}

openapiDiff {
    source = "petstore.yaml"
    from = "1.0.0"
}
