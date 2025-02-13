package com.github.bsafwen.sbtcodeartifactcreds.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

data class CodeArtifactSettings(
    var domain: String = "",
    var profile: String = "",
    var region: String = ""
)

@Service(Service.Level.PROJECT)
@State(name = "CodeArtifactSettings", storages = [Storage("CodeArtifactSettings.xml")])
class CodeArtifactSettingsComponent : PersistentStateComponent<CodeArtifactSettings> {
    private var settings = CodeArtifactSettings()

    override fun getState(): CodeArtifactSettings = settings

    override fun loadState(state: CodeArtifactSettings) {
        XmlSerializerUtil.copyBean(state, settings)
    }

    companion object {
        fun getInstance(): CodeArtifactSettingsComponent = service()
    }
}
