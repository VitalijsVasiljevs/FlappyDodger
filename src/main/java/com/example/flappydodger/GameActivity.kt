package com.example.flappydodger

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*

class GameActivity : ComponentActivity() {

    private lateinit var birdView: ImageView
    private lateinit var textViewY: TextView
    private lateinit var gameLayout: ConstraintLayout
    private lateinit var timerTextView: TextView
    private lateinit var timerHandler: Handler
    private var playerName: String? = null // vards
    private var startTime: Long = 0
    private var elapsedTime: Long = 0 // spēlēs laiks
    private var isColliding: Boolean = false // pārbauda kontaktu starp objektiem
    private val activeObjects = mutableListOf<ImageView>() // lidojošu objektu klāsts
    private var speed = 10000L // animacijas atrums, ar ko lido objekti
    private var frequency = 3000L // objektu spawnošanas biežums
    private val collisionCheckDelay = 100L // kavēšanās milisekundēs kontaktu pārbaudei
    private var isGamePaused: Boolean = false // pauze


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // iegūstam saites uz objektiem
        textViewY = findViewById(R.id.textViewY)
        gameLayout = findViewById(R.id.gameLayout)
        timerTextView = findViewById(R.id.timerTextView)
        // pieprasam nickname
        showNameInputDialog()
    }

    //
    private fun showNameInputDialog() {
        val builder = AlertDialog.Builder(this) // izveido logu ar iespēju ievadīt datus
        builder.setTitle("Enter your name")

        val input = EditText(this)
        builder.setView(input)
        //pievienoju pogu OK, lai saglabāt vārdu un sākt spēli
        builder.setPositiveButton("OK") { _, _ ->
            playerName = input.text.toString()
            // startoju spēli
            startGame()
        }
        //pievienoju pogu Cancel
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun spawnBird() {
        // Создаем новый экземпляр ImageView для птицы
        birdView = ImageView(this)

        // Устанавливаем изображение птицы
        birdView.setImageResource(R.drawable.bird) // Предполагается, что у вас есть ресурс с изображением птицы под названием "bird_image"

        // Устанавливаем начальное положение птицы
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val birdStartX = screenWidth / 2 - 1000 // Центрируем птицу по горизонтали
        val birdStartY = screenHeight / 2 - birdView.height / 2 // Центрируем птицу по вертикали
        birdView.x = birdStartX.toFloat()
        birdView.y = birdStartY.toFloat()

        // Добавляем птицу в gameLayout
        gameLayout.addView(birdView)
    }


    private fun pauseGame() {
        isGamePaused = true
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun resumeGame() {
        isGamePaused = false
        startTimer()
    }


    private fun startGame() {
        startTime = System.currentTimeMillis() //laiks
        val handler = Handler()
        // ieslēdzu laiku, cik ilgi spēlētājs izdzīvos
        startTimer()
        // spawnoju putnu, kuru spēlētājs kontroles
        spawnBird()
        // handler izmantoju, lai ar noteiktiem intervāliem spawnotos objekti, kas lidos pretī putnam
        handler.postDelayed(object : Runnable {
            override fun run() {
                // spawnoju objektus
                spawnObject()
                handler.postDelayed(this, frequency) // visu laiku samazinu intervālu
            }
        }, 3000)
        // Skārienklausītāja iestatīšana . P.s. Chat GPT palīdzeja
        findViewById<View>(android.R.id.content).setOnTouchListener { _, motionEvent ->
            // Pieskāriena notikumu apstrāde
            when (motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    // Saņemu koordinātes, kur pirksts tika nospiests
                    val y = motionEvent.y
                    // pārvietoju putnu
                    moveBird(y)
                }
            }
            true
        }
        // laika gaitā es paaugstinu spēles grūtības līmeni
        difficultyLevel()
        // bpārbaudu kontaktu starp objektiem
        startCollisionCheck()
    }

    private fun difficultyLevel() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                speed = maxOf(500, speed - 200) // samazinām aizkavi, bet ne mazāk kā 500 ms, lai paātrināt objektus
                frequency = maxOf(500, frequency - 200) // samazinām aizkavi, bet ne mazāk kā 500 ms, lai palielināt spawnošanas biežumu
                handler.postDelayed(this, 3000) // Увеличение частоты ускорения
            }
        }, 3000)
    }


    private fun createNewObject(): ImageView {
        val newObject = ImageView(this)
        newObject.setImageResource(R.drawable.test) // izmantoju bildi test.png, priekš objekta
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        newObject.layoutParams = layoutParams
        // pievienoju objektu uz scenu
        gameLayout.addView(newObject)
        return newObject
    }

    private fun spawnObject() {
        val newObject = createNewObject()
        val screenWidth = resources.displayMetrics.widthPixels // Ekrāna platuma iegūšana
        newObject.x = screenWidth.toFloat() // Objekta pozīcijas iestatīšana gar X asi:
        val random = Random() // Izveidojiet nejaušo skaitļu ģeneratoru:
        val screenHeight = resources.displayMetrics.heightPixels // Ekrāna augstuma iegūšana:
        val randomY = random.nextInt(screenHeight) // Nejaušas vērtības ģenerēšana Y koordinātei:

        newObject.y = randomY.toFloat()

        newObject.animate()
            .translationX(-screenWidth.toFloat()) // pārvietoties pa kreisi pilnekrāna režīmā
            .setDuration(speed) // kustības ilguma samazināšana (ātruma palielināšana)
            .withEndAction {
                gameLayout.removeView(newObject) // noņemt objektu pēc animācijas pabeigšanas
                activeObjects.remove(newObject) // noņemt objektu no aktīvo objektu masīva
            }
            .start()
        // pievieno objektu aktīvo objektu masīvā
        activeObjects.add(newObject)
    }

    // ieslēdzu taimeri
    private fun startTimer() {
        timerHandler = Handler()
        timerHandler.postDelayed(timerRunnable, 0)
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startTime //Pagājušā laika aprēķins
            val seconds = (elapsedTime / 1000).toInt() // Pārvērst laiku sekundēs
            val minutes = seconds / 60 // Pārvērst laiku minutes
            val remainingSeconds = seconds % 60
            timerTextView.text = String.format("%02d:%02d", minutes, remainingSeconds) // Atjaunināt teksta lauku
            timerHandler.postDelayed(this, 1000) //  izpilda vēlreiz pēc 1 sekundes
        }
    }

    private fun moveBird(y: Float) {
        if (y > 0 && y < gameLayout.height) {
            birdView.translationY = y - 100 // kustina putnu uz pirksta y asi
        }
        textViewY.text = "Y: ${y.toInt()}"
    }

    private fun startCollisionCheck() {
        val handler = Handler()
        handler.postDelayed(object : Runnable { // parbauda kontaktu starp objektiem
            override fun run() {
                checkCollisions()
                handler.postDelayed(this, collisionCheckDelay)
            }
        }, collisionCheckDelay)
    }


    private fun startNextLvl() {
        activeObjects.clear() // notīra aktīvo objektu sarakstu
        gameLayout.removeAllViews() // Noņem visus objektus no spēles izkārtojuma
        speed = 10000L // atgriež sākotnējos iestatījumus
        frequency = 3000L // atgriež sākotnējos iestatījumus
        isGamePaused = false // atgriež sākotnējos iestatījumus
        startGame() // sāk jaunu limeni
    }

    // Metode aptur spēli un parāda spēlētājam ziņojumu par rezultātu
    private fun showScoreDialog(playerName: String?) {
        pauseGame()
        val minutes = elapsedTime / 60000
        val seconds = (elapsedTime % 60000) / 1000
        val scoreMessage = "$playerName, your score in this game is $minutes minutes and $seconds seconds"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Over")
        builder.setMessage(scoreMessage)
        builder.setPositiveButton("Menu") { dialog, _ ->
            // Aizver pašreizējo darbību un atgriežas sākuma ekrānā (MainActivity)
            finish()
        }
        builder.setNegativeButton("Next lvl") { dialog, _ ->
            // sāk jaunu limeni
            startNextLvl()
        }
        builder.setCancelable(true) // Dialoglodziņa aizvēršanās novēršana, noklikšķinot ārpus tā
        builder.show()
    }


        private fun checkCollisions() {
            // Putna atrašanās vietas noteikšana ekrānā
            val birdLocation = IntArray(2)
            birdView.getLocationOnScreen(birdLocation)
            //Putna  koordinātu un izmēru iegūšana:
            val birdX = birdLocation[0]
            val birdY = birdLocation[1]
            val birdWidth = birdView.width
            val birdHeight = birdView.height

            // Pašreizēju objektu (no masīva activeObjects atrašanās vieta iegūšana ekrānā:
            for (obj in activeObjects) {
                val objLocation = IntArray(2)
                obj.getLocationOnScreen(objLocation)
                // Pašreizējā objekta koordinātu un izmēru izgūšana:
                val objX = objLocation[0]
                val objY = objLocation[1]
                val objWidth = obj.width
                val objHeight = obj.height

                // Kontakta pārbaude P.s. Chat GPT palīdzeja ar formulu
                isColliding = birdX < objX + objWidth &&
                        birdX + birdWidth > objX &&
                        birdY < objY + objHeight &&
                        birdY + birdHeight > objY


                if (isColliding) {
                    // Kontakta apstrāde
                    textViewY.text = "Collision detected!"
                    if (!isGamePaused) {
                        pauseGame()
                        showScoreDialog(playerName)
                    }
                    break
                }
            }
    }

}

