import axios, {AxiosResponse} from "axios";
import {Media} from "./Models/Media";

export const fetchAvailableMedias = async (user: string, pass: string, location: string, mediatype: string): Promise<Array<Media>> => {
    try {
        const response: AxiosResponse<Array<Media>> = await axios.get<Array<Media>>(
            `${import.meta.env.BASE_URL}api/media?location=${location}&mediatype=${mediatype}`,
            {
                headers: {
                    username: user,
                    password: pass,
                }
            }
        );
        return response.data;
    } catch (error) {
        if (axios.isAxiosError(error)) {
            if (error.response) {
                // The request was made and the server responded with a status code
                // that falls out of the range of 2xx
                console.log(error.response.data);
                console.log(error.response.status);
                console.log(error.response.headers);
                const errorResponse = error.response;
                throw Error(`Abrufen fehlgeschlagen mit Status Code '${errorResponse.status} ${errorResponse.data.error}' und Hinweis '${errorResponse.data?.message}'`)
            } else if (error.request) {
                // The request was made but no response was received
                // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
                // http.ClientRequest in node.js
                console.log(error.request);
                throw Error("Abrufen fehlgeschlagen ohne Serverantwort")
            } else {
                // Something happened in setting up the request that triggered an Error
                console.log('Error', error.message);
                throw Error(`"Abrufen fehlgeschlagen vor dem eigentlichen Absenden: ${error.message}`)
            }
        }
        throw Error(`Abrufen fehlgeschlagen aus unbekannten Gründen`)
    }
}
