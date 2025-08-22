package com.rk.update

import com.rk.libcommons.application
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.localBinDir
import java.io.File

class UpdateManager {
    fun onUpdate(){
        val initFile: File = localBinDir().child("init-host")
        if(initFile.exists()){
            initFile.delete()
        }

        if (initFile.exists().not()){
            initFile.createFileIfNot()
            initFile.writeText(application!!.assets.open("init-host.sh").bufferedReader().use { it.readText() })
        }

        // Update all distribution-specific init scripts
        val initScripts = listOf("init", "init-alpine", "init-ubuntu", "init-debian", "init-arch", "init-kali")
        val assetFiles = listOf("init.sh", "init-alpine.sh", "init-ubuntu.sh", "init-debian.sh", "init-arch.sh", "init-kali.sh")
        
        for (i in initScripts.indices) {
            val initFilex: File = localBinDir().child(initScripts[i])
            if(initFilex.exists()){
                initFilex.delete()
            }

            if (initFilex.exists().not()){
                initFilex.createFileIfNot()
                initFilex.writeText(application!!.assets.open(assetFiles[i]).bufferedReader().use { it.readText() })
            }
        }
    }
}