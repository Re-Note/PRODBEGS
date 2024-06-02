package com.example.prodiot

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

fun initializeLanguageSpinner(activity: AppCompatActivity, codeEditText: EditText, languageCallback: (String) -> Unit) {
    val languageSpinner = activity.findViewById<Spinner>(R.id.language_spinner)

    val adapter = ArrayAdapter.createFromResource(
        activity,
        R.array.languages,
        android.R.layout.simple_spinner_item
    )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    languageSpinner.adapter = adapter
    var langText = ""
    languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>?,
            selectedItemView: View?,
            position: Int,
            id: Long
        ) {
            val selectedLanguage = parentView?.getItemAtPosition(position) as String
            var defaultText = ""
            if (selectedLanguage != "선택") {
                // 언어 선택에 따른 처리 작성

                when (selectedLanguage) {
                    "C" -> { langText = "81"
                        defaultText = """
                            #include <stdio.h>

                            int main(void) {
                            	// your code goes here
                            	return 0;
                            }

                        """.trimIndent()
                    }
                    "C#" -> { langText = "86"
                        defaultText = """
                            using System;

                            public class Test
                            {
                            	public static void Main()
                            	{
                            		// your code goes here
                            	}
                            }
                        """.trimIndent()
                    }
                    "Java" -> { langText = "55"
                        defaultText = """
                /* package whatever; // don't place package name! */
                import java.util.*;
                import java.lang.*;
                import java.io.*;
                
                /* Name of the class has to be "Main" only if the class is public. */
                class main
                {
                    public static void main (String[] args) throws java.lang.Exception
                    {
                        // your code goes here
                    }
                }
            """.trimIndent()
                    }
                    "Python" -> { langText = "126"
                        defaultText = """
                            # your code goes here
                        """.trimIndent()
                    }
                    else -> { langText = "" }
                }

                // 코드 이름을 EditText에 설정
                codeEditText.setText(defaultText)

                // 선택된 언어 메시지 표시
                Toast.makeText(activity, "$selectedLanguage 언어 선택", Toast.LENGTH_SHORT).show()

                val sharedPref1 = activity.getSharedPreferences("post_data", Context.MODE_PRIVATE)
                val editor1 = sharedPref1.edit()
                editor1.putString("LangString", langText)
                Log.d("MainActivity", "Output Text: $langText")
                editor1.apply()

                val sharedPref2 = activity.getSharedPreferences("step_data", Context.MODE_PRIVATE)
                val editor2 = sharedPref2.edit()
                editor2.putString("LangString", langText)
                Log.d("MainActivity", "Output Text: $langText")
                editor2.apply()

                // 언어 정보를 콜백 함수로 전달
                languageCallback(langText)
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            // 선택되지 않았을 때의 처리
        }
    }
}

