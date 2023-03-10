package io.cloudflight.jems.server.plugin.controller

import io.cloudflight.jems.api.plugin.PluginApi
import io.cloudflight.jems.api.plugin.dto.PluginInfoDTO
import io.cloudflight.jems.api.plugin.dto.PluginTypeDTO
import io.cloudflight.jems.server.plugin.JemsPluginRegistry
import io.cloudflight.jems.server.plugin.services.update_plugin_status.UpdatePluginStatusInteractor
import org.springframework.web.bind.annotation.RestController

@RestController
class PluginController(
    private val jemsPluginRegistry: JemsPluginRegistry,
    private val updatePluginStatus: UpdatePluginStatusInteractor
) : PluginApi {

    override fun getAvailablePluginList(type: PluginTypeDTO): List<PluginInfoDTO> =
        jemsPluginRegistry.list(type.toType())
            .map { PluginInfoDTO(it.toPluginType(), it.getKey(), it.getName(), it.getVersion(), it.getDescription()) }

    override fun enablePlugin(pluginKey: String): Unit =
        updatePluginStatus.enable(pluginKey)

    override fun disablePlugin(pluginKey: String): Unit =
        updatePluginStatus.disable(pluginKey)
}
