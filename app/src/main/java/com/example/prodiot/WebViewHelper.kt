package com.example.prodiot

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class PostWebViewHelper(private val context: Context) {

    private fun escapeCodeString(code: String): String {
        // 특수 문자나 특수 기호를 이스케이프 처리
        return code
            .replace("\\", "\\\\")  // 역슬래시
            .replace("\"", "\\\"")  // 큰따옴표
            .replace("\'", "\\\'")  // 작은따옴표
            .replace("\n", "\\n")   // 줄바꿈
            .replace("\r", "")      // 캐리지 리턴
    }

    fun configureWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
    }

    fun submitCode(webView: WebView, progressDialog: CustomProgressDialog) {
        val sharedPreferences = context.getSharedPreferences("post_data", Context.MODE_PRIVATE)
        val langString = sharedPreferences.getString("LangString", "")
        val codeString = sharedPreferences.getString("CodeString", "")
        val inputString = sharedPreferences.getString("InputString", "")
        webView.webViewClient = object : WebViewClient() {
            var onPageFinishedCalled = false // onPageFinished가 한 번만 호출되도록 제어 변수 추가

            override fun onPageFinished(view: WebView?, url: String?) {
                if (onPageFinishedCalled) {
                    return // 이미 호출되었으면 무시
                }
                onPageFinishedCalled = true // 호출 플래그 설정

                val js1 = """document.getElementById('_lang').value = '$langString';"""
                view?.evaluateJavascript(js1, null)
                val js2 = """var textarea = document.getElementById('file');"""
                view?.evaluateJavascript(js2, null)
                val js3 = """textarea.value = "${escapeCodeString(codeString.toString())}";"""
                view?.evaluateJavascript(js3, null)
                val js4 = """var textarea = document.getElementById('input');"""
                view?.evaluateJavascript(js4, null)
                val js5 = """textarea.value = "${escapeCodeString(inputString.toString())}";"""
                view?.evaluateJavascript(js5, null)
                val js6 = """document.getElementsByName('Submit')[0].click();"""
                view?.evaluateJavascript(js6, null)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(1000)
                    var outputText: String
                    var compileText: String
                    // 새로운 변수 추가
                    var isCompilationError = false
                    var isSuccess = false
                    do {
                        delay(1000)
                        val currentUrl = webView.url
                        val docs = withContext(Dispatchers.IO) {
                            Jsoup.connect(currentUrl).get()
                        }
                        if (docs.select("span.info.red[title='Compilation error']").isNotEmpty() || docs.select("span.info.green[title='Success']").isNotEmpty()) {
                            if (docs.select("span.info.red[title='Compilation error']").isNotEmpty()) {
                                isCompilationError = true
                            }
                            if (docs.select("span.info.green[title='Success']").isNotEmpty()) {
                                isSuccess = true
                            }
                            if (isCompilationError || isSuccess) {
                                outputText = withContext(Dispatchers.IO) {
                                    docs.select("#output-text").text()
                                }
                                compileText = withContext(Dispatchers.IO) {
                                    docs.select("#view_cmperr_content").text()
                                }
                                val textView = (context as AppCompatActivity).findViewById<TextView>(R.id.output_edittext)
                                textView.text = outputText

                                val textView2 = (context as AppCompatActivity).findViewById<TextView>(R.id.compilation_edittext)
                                textView2.text = compileText

                                Log.d("WebViewHelper.kt", "compile check: \n$codeString\n, $inputString\n, $outputText\n, $compileText\n")
                            }
                        }
                    } while (!isCompilationError && !isSuccess);
                    progressDialog.dismiss()
                }
            }
        }
        webView.loadUrl("https://ideone.com/")
    }
}

class StepWebViewHelper(private val context: Context) {
    private fun escapeCodeString(code: String): String {
        // 특수 문자나 특수 기호를 이스케이프 처리
        return code
            .replace("\\", "\\\\")  // 역슬래시
            .replace("\"", "\\\"")  // 큰따옴표
            .replace("\'", "\\\'")  // 작은따옴표
            .replace("\n", "\\n")   // 줄바꿈
            .replace("\r", "")      // 캐리지 리턴
    }

    fun configureWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
    }

    fun submitCode(webView: WebView, progressDialog: CustomProgressDialog, callback: () -> Unit) {
        val sharedPreferences = context.getSharedPreferences("step_data", Context.MODE_PRIVATE)
        val langString = sharedPreferences.getString("LangString", "")
        val codeString = sharedPreferences.getString("CodeString", "")
        val inputString = sharedPreferences.getString("InputString", "")
        webView.webViewClient = object : WebViewClient() {
            var onPageFinishedCalled = false // onPageFinished가 한 번만 호출되도록 제어 변수 추가

            override fun onPageFinished(view: WebView?, url: String?) {
                if (onPageFinishedCalled) {
                    return // 이미 호출되었으면 무시
                }
                onPageFinishedCalled = true // 호출 플래그 설정

                val js1 = """document.getElementById('_lang').value = '$langString';"""
                view?.evaluateJavascript(js1, null)
                val js2 = """var textarea = document.getElementById('file');"""
                view?.evaluateJavascript(js2, null)
                val js3 = """textarea.value = "${escapeCodeString(codeString.toString())}";"""
                view?.evaluateJavascript(js3, null)
                val js4 = """var textarea = document.getElementById('input');"""
                view?.evaluateJavascript(js4, null)
                val js5 = """textarea.value = "${escapeCodeString(inputString.toString())}";"""
                view?.evaluateJavascript(js5, null)
                val js6 = """document.getElementsByName('Submit')[0].click();"""
                view?.evaluateJavascript(js6, null)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(1000)
                    var outputText: String
                    var compileText: String
                    // 새로운 변수 추가
                    var isCompilationError = false
                    var isSuccess = false
                    do {
                        delay(1000)
                        val currentUrl = webView.url
                        val docs = withContext(Dispatchers.IO) {
                            Jsoup.connect(currentUrl).get()
                        }
                        if (docs.select("span.info.red[title='Compilation error']").isNotEmpty() || docs.select("span.info.green[title='Success']").isNotEmpty()) {
                            if (docs.select("span.info.red[title='Compilation error']").isNotEmpty()) {
                                isCompilationError = true
                            }
                            if (docs.select("span.info.green[title='Success']").isNotEmpty()) {
                                isSuccess = true
                            }
                            if (isCompilationError || isSuccess) {
                                outputText = withContext(Dispatchers.IO) {
                                    docs.select("#output-text").text()
                                }
                                compileText = withContext(Dispatchers.IO) {
                                    docs.select("#view_cmperr_content").text()
                                }
                                val textView = (context as AppCompatActivity).findViewById<TextView>(R.id.output_edittext)
                                textView.text = outputText

                                val textView2 = (context as AppCompatActivity).findViewById<TextView>(R.id.compilation_edittext)
                                textView2.text = compileText

                                Log.d("WebViewHelper.kt", "compile check: \n$codeString\n, $inputString\n, $outputText\n, $compileText\n")
                            }
                        }
                    } while (!isCompilationError && !isSuccess);

                    progressDialog.dismiss()
                    // 완료를 나타내기 위해 콜백 함수를 호출합니다
                    callback()
                }
            }
        }
        webView.loadUrl("https://ideone.com/")
    }
}


