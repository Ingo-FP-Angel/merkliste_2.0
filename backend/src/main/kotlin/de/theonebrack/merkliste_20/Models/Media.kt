package de.theonebrack.merkliste_20.Models

/**
 * Representation of one entry on your 'Merkliste'
 *
 * @property availability negative values indicate: -1 = not available at chosen location, -2 = availability info currently unavailable, -3 = details page with availability information could not be retrieved
 */
data class Media(val name: String, val author: String?, val type: String, val signature: String, val url: String, var availability: Int)
