package io.github.jixiaoyong.pages.settings

import ApkSigner
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import io.github.jixiaoyong.pages.signapp.DropBoxPanel
import io.github.jixiaoyong.utils.FileChooseUtil
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.utils.StorageKeys
import io.github.jixiaoyong.widgets.ButtonWidget
import io.github.jixiaoyong.widgets.InfoItemWidget
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JPanel

/**
 * @author : jixiaoyong
 * @description ：设置页面
 *
 * 配置apksigner，java，keytool等环境变量
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */
@Preview
@Composable
fun PageSettingInfo(window: ComposeWindow, settings: SettingsTool) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val apkSign by settings.apkSigner.collectAsState(null)
    val zipAlign by settings.zipAlign.collectAsState(null)
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        var resetSignInfo by remember { mutableStateOf(false) }
        var resetApkTools by remember { mutableStateOf(false) }
        var resetSignTypes by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = {
            showResetDialog = false
        }, confirmButton = {
            ButtonWidget(onClick = {
                showResetDialog = false
                if (resetSignInfo) {
                    settings.save(StorageKeys.SIGN_INFO_LIST, null)
                    settings.save(StorageKeys.SIGN_INFO_SELECT, null)
                }
                if (resetApkTools) {
                    settings.save(StorageKeys.APK_SIGNER_PATH, null)
                    settings.save(StorageKeys.ZIP_ALIGN_PATH, null)
                }
                if (resetSignTypes) {
                    settings.save(StorageKeys.SIGN_TYPE_LIST, null)
                }
            }, title = "确定")

        }, title = {
            Text("确定重置吗")
        }, text = {
            Column {
                Text("重置会清除以下内容，请谨慎操作")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetSignInfo,
                        onCheckedChange = { resetSignInfo = !resetSignInfo })
                    Text("签名信息")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetApkTools,
                        onCheckedChange = { resetApkTools = !resetApkTools })
                    Text("签名工具配置(不会删除文件)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetSignTypes,
                        onCheckedChange = { resetSignTypes = !resetSignTypes })
                    Text("签名方案")
                }
            }
        })
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DropBoxPanel(window,
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(15.dp)
                    .clickable {
                        scope.launch {
                            val oldDirectory = apkSign?.substringBeforeLast(File.separator)
                            val chooseFileName =
                                FileChooseUtil.chooseSignDirectory(
                                    window,
                                    "请选择build-tools目录",
                                    oldDirectory
                                )
                            if (chooseFileName.isNullOrBlank()) {
                                scaffoldState.snackbarHostState.showSnackbar("请选择build-tools目录")
                            } else {
                                val result = ApkSigner.init(chooseFileName)
                                saveApkSigner(settings, ApkSigner.apkSignerPath)
                                saveZipAlign(settings, ApkSigner.zipAlignPath)
                                scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                            }
                        }
                    },
                component = JPanel(),
                onFileDrop = {
                    scope.launch {
                        val result = ApkSigner.init(it.first())

                        saveApkSigner(settings, ApkSigner.apkSignerPath)
                        saveZipAlign(settings, ApkSigner.zipAlignPath)

                        scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                    }
                }) {
                Text(
                    text = "请拖拽Android SDK的build-tools文件夹到这里，以一次性修改apkSigner和zipAlign目录",
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }

            InfoItemWidget("apk signer目录", apkSign ?: "尚未初始化", onClick = {
                scope.launch {
                    val chooseFileName =
                        FileChooseUtil.chooseSignFile(window, "请选择apksigner文件")
                    if (chooseFileName.isNullOrBlank()) {
                        scaffoldState.snackbarHostState.showSnackbar("请选择apksigner文件")
                    } else {
                        val result = ApkSigner.setupApkSigner(chooseFileName)
                        saveApkSigner(settings, ApkSigner.apkSignerPath)
                        scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                    }
                }

            })
            InfoItemWidget("zip align目录", zipAlign ?: "尚未初始化", onClick = {
                scope.launch {
                    val chooseFileName = FileChooseUtil.chooseSignFile(window, "请选择zipAlign文件")
                    if (chooseFileName.isNullOrBlank()) {
                        scaffoldState.snackbarHostState.showSnackbar("请选择zipAlign文件")
                    } else {
                        val result = ApkSigner.setupZipAlign(chooseFileName)
                        saveZipAlign(settings, ApkSigner.zipAlignPath)
                        scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                    }
                }
            })

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {

                val modifier = Modifier.size(250.dp, 50.dp)
                    .background(
                        MaterialTheme.colors.secondary,
                        shape = RoundedCornerShape(15.dp)
                    ).padding(horizontal = 15.dp, vertical = 5.dp)

                ButtonWidget(onClick = {
                    showResetDialog = true
                }, title = "重置", modifier = modifier)
            }
        }
    }

}

private fun saveApkSigner(settings: SettingsTool, apkSigner: String?) {
    settings.save(StorageKeys.APK_SIGNER_PATH, apkSigner)
}

private fun saveZipAlign(settings: SettingsTool, zipAlign: String?) {
    settings.save(StorageKeys.ZIP_ALIGN_PATH, zipAlign)
}