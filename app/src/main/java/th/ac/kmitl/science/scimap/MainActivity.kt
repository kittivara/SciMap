package th.ac.kmitl.science.scimap

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import retrofit2.Call
import retrofit2.Callback
import th.ac.kmitl.science.scimap.service.Area

import th.ac.kmitl.science.scimap.service.KmitlMapService
import android.view.ScaleGestureDetector
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.net.URL
import android.annotation.SuppressLint
import android.graphics.Matrix
import th.ac.kmitl.science.scimap.service.Building
import android.widget.Toast
import th.ac.kmitl.science.scimap.service.Coordinate
import android.graphics.drawable.Drawable
import android.graphics.Matrix.MSCALE_Y
import android.graphics.Matrix.MSCALE_X
import kotlinx.android.synthetic.main.activity_main.*
import th.ac.kmitl.science.scimap.R.id.imageView
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.R.attr.scaleY
import android.R.attr.scaleX




class MainActivity : AppCompatActivity() {

    lateinit var mImageView: ImageView
    lateinit var mProgress: ProgressBar
    private var mProgressToggle: Boolean = false
    var mBuildings: Array<Building>? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mImageView = findViewById(R.id.imageView)
        mProgress = findViewById(R.id.progressBar)

        getAreaImage()
        getAreaBuildings()

        mImageView.setOnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            var point = getImageCoordinate(mImageView, Coordinate(x, y))

            val found = searchPolygon(point)
            Log.i("SciMap",point.x.toString() + "," + point.y.toString())
            if(found != null) {
                Log.i("SciMap","Selected building: " + found?.DisplayName)
            }
            false
        }
    }

    fun getImageCoordinate(imageView: ImageView, point: Coordinate): Coordinate {

        // Get image dimensions
        // Get image matrix values and place them in an array
        val f = FloatArray(9)
        imageView.imageMatrix.getValues(f)

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        val d = imageView.drawable
        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight

        // Calculate the actual dimensions
        val actW = Math.round(origW * scaleX)
        val actH = Math.round(origH * scaleY)

        // Get image position
        // We assume that the image is centered into ImageView
        val imgViewW = imageView.width
        val imgViewH = imageView.height

        val top = (imgViewH - actH) / 2
        val left = (imgViewW - actW) / 2

        // Calculate the actual dimensions
        val x = point.x - left
        val y = point.y - top

        return Coordinate(x, y)
    }

    private fun getAreaBuildings() {
        toggleProgress()
        var service = KmitlMapService.create()
        var call = service.getBuildingsOfArea(1)
        call.enqueue(object : Callback<Array<Building>> {

            override fun onResponse(call: Call<Array<Building>>, response: retrofit2.Response<Array<Building>>?) {
                if (response != null) {
                    toggleProgress()
                    mBuildings = response?.body()
                }
            }

            override fun onFailure(call: Call<Array<Building>>, t: Throwable) {
                toggleProgress()
            }
        })
    }

    private fun searchPolygon(point: Coordinate): Building? {
        for (building in mBuildings!!.iterator()) {
            if (contains(building.PolygonArea, point))
                return building
        }
        return null
    }

    fun contains(points: Array<Coordinate>, test: Coordinate): Boolean {
        var i = 0
        var j: Int
        var result = false
        j = points.size - 1
        while (i < points.size) {
            if (points[i].y > test.y != points[j].y > test.y
                    && test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x) {
                result = !result
            }
            j = i++
        }
        return result
    }

    fun getAreaImage() {
        toggleProgress()
        var service = KmitlMapService.create()
        var call = service.getArea(1)
        call.enqueue(object : Callback<Area> {

            override fun onResponse(call: Call<Area>, response: retrofit2.Response<Area>?) {
                if (response != null) {
                    toggleProgress()
                    val requested = response.body()
                    Log.i("MainActivity", "responsed :" + requested?.DisplayName)
                    val task = DownLoadImageTask(mImageView)
                    task.execute(requested?.AreaPlanImage)
                }
            }

            override fun onFailure(call: Call<Area>, t: Throwable) {
                toggleProgress()
            }
        })
    }


    fun toggleProgress() {
        if(!mProgressToggle)
            mProgress.visibility = View.VISIBLE
        else
            mProgress.visibility = View.GONE
        mProgressToggle = !mProgressToggle
    }

    private inner class DownLoadImageTask(internal var imageView: ImageView) : AsyncTask<String, Void, Bitmap>() {

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        override fun doInBackground(vararg urls: String): Bitmap? {
            val urlOfImage = urls[0]
            var logo: Bitmap? = null
            try {
                val `is` = URL(urlOfImage).openStream()
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(`is`)
            } catch (e: Exception) { // Catch the download exception
                e.printStackTrace()
            }

            return logo
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        override fun onPostExecute(result: Bitmap) {
            imageView.setImageBitmap(result)
        }
    }
}
