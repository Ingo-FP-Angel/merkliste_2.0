import axios, {AxiosResponse} from "axios";
import {Media} from "./Models/Media";

export const fetchAvailableMedias = async (user: string, pass: string, location: string, mediatype: string): Promise<Array<Media>> => {
    try {
        const backendBaseUrl = process.env.NODE_ENV == "production" ? `${import.meta.env.BASE_URL}/api` : "http://localhost:8080/api";

        const response: AxiosResponse<Array<Media>> = await axios.get<Array<Media>>(
            `${backendBaseUrl}/media?location=${location}&mediatype=${mediatype}`,
            {
                headers: {
                    username: user,
                    password: pass,
                }
            }
        );
        return response.data;
    } catch (e) {
        if (axios.isAxiosError(e)) {
            const errorResponse = e.response?.data;
            throw Error(`Abrufen fehlgeschlagen mit Status Code '${errorResponse.status} ${errorResponse.error}' und Hinweis '${errorResponse?.message}'`)
        }
        throw Error(`Abrufen fehlgeschlagen aus unbekannten Gründen`)
    }
}
