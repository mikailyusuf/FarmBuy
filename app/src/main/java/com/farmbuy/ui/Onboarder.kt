package com.farmbuy.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.farmbuy.R
import com.farmbuy.auth.AuthActivity
import com.farmbuy.buyer.ui.BuyersActivity
import com.farmbuy.farmer.FarmersActivity
import com.google.firebase.auth.FirebaseAuth
import com.mahmoud.onboardingview.OnBoardingScreen
import com.mahmoud.onboardingview.OnBoardingView

class Onboarder : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "farm_buy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarder)


        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)

        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser !== null) {

            when {
                sharedPref.getString(PREF_NAME, null) == "farmer" -> {
                    val intent = Intent(this, FarmersActivity::class.java)
                    startActivity(intent)
                }
                sharedPref.getString(PREF_NAME, null) == "buyer" -> {

                    val intent = Intent(this, BuyersActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    startActivity(Intent(this, AuthActivity::class.java))
                }
            }

        } else {
            val onBoardingView = findViewById<OnBoardingView>(R.id.onboardingView)
            var screens = arrayListOf<OnBoardingScreen>()
            screens.add(
                OnBoardingScreen(
                    titleText = "Hunger",
                    subTitleText = "An estimated 820 million people do not have enough to eat,\n" +
                            " up from 811 million last year - third year in a row the number increase: UN report",
                    screenBGColor = R.color.onboarding_bg,
                    drawableResId = R.drawable.hunger1
                )
            )
            screens.add(
                OnBoardingScreen(
                    titleText = "SDG GOAL 2",
                    subTitleText = "Goal 2 seeks sustainable solutions to end hunger in all its forms by 2030 and to achieve food security.\n" +
                            "The aim is to ensure that everyone everywhere has enough good-quality food to lead a healthy life.",
                    drawableResId = R.drawable.sdg1,
                    screenBGColor = R.color.onboarding_bg

                    )
            )
            screens.add(
                OnBoardingScreen(
                    titleText = "SDG GOAL 2",
                    subTitleText = "Achieving this Goal will require better access to food and the widespread promotion of sustainable agriculture",
                    drawableResId = R.drawable.sdg1,
                    screenBGColor = R.color.onboarding_bg

                    )
            )
            screens.add(
                OnBoardingScreen(
                    titleText = "Farmers",
                    subTitleText = "By Connecting Local Framers to Buyers We can have a massive Reduction in World Hunger",
                    screenBGColor = R.color.onboarding_bg,
                    drawableResId = R.drawable.farmer1
                )
            )

            screens.add(
                OnBoardingScreen(
                    titleText = "FarmBuy",
                    subTitleText = "The Idea behind this App is to help buyers get fresh produce and buy directly from farmers, \n" +
                            "it will also help farmers sell faster, thereby reducing costs and improving health and Nutrition of people ,\n" +
                            "farmer stand a chance of selling products fast, and people stand a chance of getting cheaper foods thereby reducing Hunger",
                    screenBGColor = R.color.onboarding_bg,
                    drawableResId = R.drawable.food1
                )
            )

            onBoardingView.setScreens(screens)

            onBoardingView.onEnd {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }

            onBoardingView.onFinish {
//                Toast.makeText(this, "OnBoarding last screen", Toast.LENGTH_SHORT).show()
                /* return false will not trigger this action again(on swipe back ) ,
            true will trigger it with every swipe to last screen ,
            may used for showing some animation or something */
                return@onFinish false

            }


        }
    }


}