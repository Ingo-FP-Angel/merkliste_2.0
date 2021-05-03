import React, {useState} from 'react';
import {Box, Button, LinearProgress, TextField} from "@material-ui/core";
import './App.css';
import {Media} from "./Models/Media";
import {fetchAvailableMedias} from "./Api";
import {MediaList} from "./Components/MediaList";
import {LocationSelect} from "./Components/LocationSelect";
import {MediatypeSelect} from "./Components/MediatypeSelect";

const App = () => {
    const [loading, setLoading] = useState({isLoading: false, errorMessage: ""});
    const [media, setMedia] = useState<Array<Media>>([]);
    const [formValues, setFormValues] = useState({
        user: "",
        pass: "",
        location: "Zentralbibliothek",
        mediatype: "all",
    });

    const handleUserChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormValues({...formValues, user: e.target.value});
    }
    const handlePassChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormValues({...formValues, pass: e.target.value});
    }
    const handleLocationChange = (location: string) => {
        setFormValues({...formValues, location: location});
    }
    const handleMediatypeChange = (mediatype: string) => {
        setFormValues({...formValues, mediatype: mediatype});
    }

    const handleSubmit = async () => {
        setLoading({isLoading: true, errorMessage: ""});
        try {
            const media = await fetchAvailableMedias(formValues.user, formValues.pass, formValues.location, formValues.mediatype);
            setMedia(media);
            setLoading({isLoading: false, errorMessage: ""});
        } catch (e) {
            console.log("Error in App: ", e.message);
            setLoading({isLoading: false, errorMessage: e.message || "Fehler beim Abrufen"});
        }
    };

    return (
        <div>
            <h1>Merkliste 2.0</h1>

            <form autoComplete="off" onSubmit={handleSubmit}>
                <Box display="flex" flexDirection="column">
                    <TextField required label="Nummer der Kundenkarte"
                               value={formValues.user} onChange={handleUserChange}/>
                    <TextField required label="Passwort" type="password"
                               value={formValues.pass} onChange={handlePassChange}/>
                    <LocationSelect
                        location={formValues.location} onSelect={handleLocationChange}/>
                    <MediatypeSelect
                        mediatype={formValues.mediatype} onSelect={handleMediatypeChange}/>
                </Box>
                <Button variant="outlined" color="primary"
                        disabled={loading.isLoading || !formValues.user || !formValues.pass}
                        onClick={handleSubmit}>
                    {loading.isLoading ? "Lade..." : "Medien abrufen"}
                </Button>
            </form>

            {loading.isLoading && <LinearProgress/>}

            {loading.errorMessage &&
            <p>
                {loading.errorMessage}
            </p>}

            <h3>Verfügbare Medien</h3>
            <MediaList media={media} isLoading={loading.isLoading}/>
        </div>
    );
}

export default App;
