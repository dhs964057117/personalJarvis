package com.cabbage.jarvis.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.cabbage.jarvis.App
import com.cabbage.jarvis.data.ChannelListBean
import com.cabbage.jarvis.data.Constant
import com.cabbage.jarvis.data.Constant.LIVE2D_BASE_PATH
import com.cabbage.jarvis.data.Constant.LOCAL_MODEL_ATRI
import com.cabbage.jarvis.data.Constant.LOCAL_MODEL_HIYORI
import com.cabbage.jarvis.data.Constant.LOCAL_MODEL_KURISU
import com.cabbage.jarvis.data.Constant.VITS_BASE_PATH
import com.chatwaifu.vits.utils.file.copyAssets2Local
import kotlinx.coroutines.suspendCancellableCoroutine
import com.cabbage.jarvis.R
import com.cabbage.jarvis.data.safeResume
import java.io.File

/**
 * Description: LocalModelManager
 * Author: Voine
 * Date: 2023/2/25
 */
class LocalModelManager() {
    private val sp: SharedPreferences by lazy {
        App.application.getSharedPreferences(
            Constant.SAVED_STORE,
            Context.MODE_PRIVATE
        )
    }

    suspend fun initInnerModel(context: Context, finalModelList: MutableList<ChannelListBean>) {
        // copy live2d
        val live2DModelList = context.assets.list(LIVE2D_BASE_PATH)
        live2DModelList?.forEach { modelName ->
            val srcPath = "$LIVE2D_BASE_PATH/$modelName"
            suspendCancellableCoroutine {
                App.application.copyAssets2Local(
                    deleteIfExists = true,
                    srcPath = srcPath,
                    desPath = App.baseAppDir + File.separator
                ) { isSuccess: Boolean, absPath: String ->
                    if (!isSuccess) {
                        AT.toastError("copy  live2d $modelName to disk failed....")
                        return@copyAssets2Local
                    }
                    finalModelList.add(
                        ChannelListBean(
                            avatarDrawable = getAvatarDrawableFromName(modelName),
                            characterName = modelName,
                            characterPath = App.baseAppDir + File.separator + srcPath
                        )
                    )
                }
                it.safeResume(Unit)
            }
        }

        //copy vits
        val vitsModelList = context.assets.list(VITS_BASE_PATH)
        vitsModelList?.forEach { vitsName ->
            val srcPath = "$VITS_BASE_PATH/$vitsName"
            suspendCancellableCoroutine {
                App.application.copyAssets2Local(
                    deleteIfExists = true,
                    srcPath = srcPath,
                    desPath = App.baseAppDir + File.separator
                ) { isSuccess: Boolean, absPath: String ->
                    if (!isSuccess) {
                        AT.toastError("copy vits $vitsName to disk failed....")
                        return@copyAssets2Local
                    }
                    finalModelList.find { target ->
                        target.characterName == vitsName
                    }.apply {
                        this?.characterVitsPath =
                            App.baseAppDir + File.separator + srcPath
                    }
                }
                it.safeResume(Unit)
            }
        }
    }

    private fun getAvatarDrawableFromName(name: String): Int {
        return when {
            name == LOCAL_MODEL_HIYORI -> R.drawable.error_view
            name == LOCAL_MODEL_KURISU -> R.drawable.error_view
            name == LOCAL_MODEL_ATRI -> R.drawable.error_view
            else -> R.drawable.error_view
        }
    }


    fun initExternalModel(context: Context, finalModelList: MutableList<ChannelListBean>) {
        val chatWaifuExternalDir = Environment.getExternalStorageDirectory().path
        val externalLive2dModels =
            File(chatWaifuExternalDir, Constant.EXTEND_LIVE2D_PATH).listFiles()
        val externalVITSModels = File(chatWaifuExternalDir, Constant.EXTEND_VITS_PATH).listFiles()
        externalLive2dModels?.forEach { live2dModel ->
            val relativeVITSModel = externalVITSModels?.find { it.name == live2dModel.name }
            finalModelList.add(
                ChannelListBean(
                    avatarDrawable = R.drawable.error_view,
                    characterPath = live2dModel.absolutePath,
                    characterName = live2dModel.name,
                    characterVitsPath = relativeVITSModel?.absolutePath ?: "",
                    fromExternal = true
                )
            )
        }
    }

//    fun getModelSystemSetting(modelName: String): String? {
//        return when (modelName) {
//            LOCAL_MODEL_HIYORI -> {
//                sp.getString(Constant.SAVED_HIYORI_SETTING, null)?.let {
//                    it.ifBlank { null }
//                } ?:  ChatWaifuApplication.context.resources.getString(R.string.default_system_hiyori)
//            }
//            LOCAL_MODEL_KURISU -> {
//                sp.getString(Constant.SAVED_AMADEUS_SETTING, null)?.let {
//                    it.ifBlank { null }
//                } ?:  ChatWaifuApplication.context.resources.getString(R.string.default_system_amadeus)
//            }
//            LOCAL_MODEL_ATRI -> {
//                sp.getString(Constant.SAVED_ATRI_SETTING, null)?.let {
//                    it.ifBlank { null }
//                } ?: ChatWaifuApplication.context.resources.getString(R.string.default_system_atri)
//            }
//            else -> {
//                sp.getString(Constant.SAVED_EXTERNAL_SETTING, null)?.let {
//                    it.ifBlank { null }
//                }
//            }
//        }
//    }

    /**
     * 由于奇妙的权限问题，即使申请了权限， listFile 也返回空数组，但指定具体路径又能找得到文件，所以暂时这么处理
     */
    fun getVITSModelFiles(basePath: String): List<File>? {
        val files = File(basePath).listFiles()
        if (!files.isNullOrEmpty()) {
            return files.toList()
        }
        val resultList = mutableListOf<File>()
        vitsFileArr.forEach {
            val file = File(basePath, it)
            if (file.exists()) {
                resultList.add(file)
            }
        }
        return resultList
    }

    companion object {
        val vitsFileArr= listOf(
            "config.json",
            "dec.ncnn.bin",
            "dp.ncnn.bin",
            "flow.ncnn.bin",
            "flow.reverse.ncnn.bin",
            "emb_g.bin",
            "emb_t.bin",
            "enc_p.ncnn.bin",
            "enc_q.ncnn.bin"
        )
    }
}