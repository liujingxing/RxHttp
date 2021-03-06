package com.example.httpsender.fragment

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.rxLifeScope
import com.example.httpsender.R
import com.example.httpsender.databinding.CoroutineFragmentBinding
import com.example.httpsender.entity.*
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.google.gson.Gson
import rxhttp.await
import rxhttp.awaitString
import rxhttp.toBitmap
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toResponse
import java.util.*

/**
 * 使用Coroutine+OkHttp发请求
 * User: ljx
 * Date: 2020/4/24
 * Time: 18:16
 */
class CoroutineFragment : BaseFragment<CoroutineFragmentBinding>(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.coroutine_fragment)
    }

    override fun CoroutineFragmentBinding.onViewCreated(savedInstanceState: Bundle?) {
        click = this@CoroutineFragment
    }

    private fun CoroutineFragmentBinding.bitmap(view: View) {
        rxLifeScope.launch({
            val imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888"
            val bitmap = RxHttp.get(imageUrl).toBitmap().await()
            tvResult.background = BitmapDrawable(bitmap)
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    //发送Get请求，获取文章列表
    private fun CoroutineFragmentBinding.sendGet(view: View) {
        rxLifeScope.launch({
            val pageList = RxHttp.get("/article/list/0/json")
                .toResponse<PageList<Article>>()
                .await()
            tvResult.text = Gson().toJson(pageList)
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    //发送Post表单请求,根据关键字查询文章
    private fun CoroutineFragmentBinding.sendPostForm(view: View) {
        rxLifeScope.launch({
            val pageList = RxHttp.postForm("/article/query/0/json")
                .add("k", "性能优化")
                .toResponse<PageList<Article>>()
                .await()
            tvResult.text = Gson().toJson(pageList)
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    //发送Post Json请求，此接口不通，仅用于调试参数
    private fun CoroutineFragmentBinding.sendPostJson(view: View) {
        rxLifeScope.launch({
            //发送以下User对象
            /*
               {
                   "name": "张三",
                   "sex": 1,
                   "height": 180,
                   "weight": 70,
                   "interest": [
                       "羽毛球",
                       "游泳"
                   ],
                   "location": {
                       "latitude": 30.7866,
                       "longitude": 120.6788
                   },
                   "address": {
                       "street": "科技园路.",
                       "city": "江苏苏州",
                       "country": "中国"
                   }
               }
             */
            val interestList: MutableList<String> = ArrayList() //爱好
            interestList.add("羽毛球")
            interestList.add("游泳")
            val address = "{\"street\":\"科技园路.\",\"city\":\"江苏苏州\",\"country\":\"中国\"}"
            val result = RxHttp.postJson("/article/list/0/json")
                .add("name", "张三")
                .add("sex", 1)
                .addAll("{\"height\":180,\"weight\":70}") //通过addAll系列方法添加多个参数
                .add("interest", interestList) //添加数组对象
                .add("location", Location(120.6788, 30.7866)) //添加位置对象
                .addJsonElement("address", address) //通过字符串添加一个对象
                .awaitString()
            tvResult.text = result
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    //发送Post JsonArray请求，此接口不通，仅用于调试参数
    private fun CoroutineFragmentBinding.sendPostJsonArray(view: View) {
        rxLifeScope.launch({
            //发送以下Json数组
            /*
               [
                   {
                       "name": "张三"
                   },
                   {
                       "name": "李四"
                   },
                   {
                       "name": "王五"
                   },
                   {
                       "name": "赵六"
                   },
                   {
                       "name": "杨七"
                   }
               ]
             */
            val names: MutableList<Name?> = ArrayList()
            names.add(Name("赵六"))
            names.add(Name("杨七"))
            val result = RxHttp.postJsonArray("/article/list/0/json")
                .add("name", "张三")
                .add(Name("李四"))
                .addJsonElement("{\"name\":\"王五\"}")
                .addAll(names)
                .awaitString()
            tvResult.text = result
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    //使用XmlConverter解析数据，此接口返回数据太多，会有点慢
    private fun CoroutineFragmentBinding.xmlConverter(view: View) {
        rxLifeScope.launch({
            val dataXml = RxHttp.get("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni")
                .setXmlConverter()
                .await<NewsDataXml>()
            tvResult.text = Gson().toJson(dataXml)
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    //使用XmlConverter解析数据
    private fun CoroutineFragmentBinding.fastJsonConverter(view: View) {
        rxLifeScope.launch({
            val pageList = RxHttp.get("/article/list/0/json")
                .setFastJsonConverter()
                .toResponse<PageList<Article>>()
                .await()
            tvResult.text = Gson().toJson(pageList)
        }, {
            tvResult.text = it.errorMsg
            //失败回调
            it.show()
        })
    }

    private fun CoroutineFragmentBinding.clearLog(view: View) {
        tvResult.text = ""
        tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onClick(v: View) = mBinding.run {
        when (v.id) {
            R.id.bitmap -> bitmap(v)
            R.id.sendGet -> sendGet(v)
            R.id.sendPostForm -> sendPostForm(v)
            R.id.sendPostJson -> sendPostJson(v)
            R.id.sendPostJsonArray -> sendPostJsonArray(v)
            R.id.xmlConverter -> xmlConverter(v)
            R.id.fastJsonConverter -> fastJsonConverter(v)
            R.id.bt_clear -> clearLog(v)
        }
    }
}