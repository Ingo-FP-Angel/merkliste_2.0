package de.theonebrack.merkliste_20.Config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(MerklisteProperties::class)
class MerklisteConfiguration {

    @Autowired
    private val merklisteProperties: MerklisteProperties? = null
}