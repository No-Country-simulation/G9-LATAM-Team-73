# Model artifacts (Dev 2 / Ciencia de Datos)

Place the exported ONNX model here as:

```
techmind_classifier.onnx
```

Expected export (from the TechMind notebook): scikit-learn `Pipeline(TfidfVectorizer, LogisticRegression)` converted with `skl2onnx` using `StringTensorType`, so the Java runtime can send cleaned text strings directly.

Until the `.onnx` file is provided, `OnnxModelService` uses the keyword fallback classifier (`techmind.ml.fallback-enabled=true`).
