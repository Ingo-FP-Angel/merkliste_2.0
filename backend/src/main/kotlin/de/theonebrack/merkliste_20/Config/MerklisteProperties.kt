package de.theonebrack.merkliste_20.Config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("merkliste")
data class MerklisteProperties(var baseUrl: String)