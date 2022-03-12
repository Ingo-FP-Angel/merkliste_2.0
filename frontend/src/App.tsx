import React, {useState} from 'react';
import {Box, Button, LinearProgress, TextField} from "@mui/material";
import './App.css';
import {Media} from "./Models/Media";
import {fetchAvailableMedias} from "./Api";
import {MediaList} from "./Components/MediaList";
import {LocationSelect} from "./Components/LocationSelect";
import {MediatypeSelect} from "./Components/MediatypeSelect";
import {makeStyles} from "@mui/styles";

const useStyles = makeStyles(theme => ({
    form: {
        marginTop: theme.spacing(2)
    },
}));

const localStorageDefaultLocation = "merkliste:defaultLocation"

const App = () => {
    const classes = useStyles();
    const [loading, setLoading] = useState({isLoading: false, errorMessage: ""});
    const [media, setMedia] = useState<Array<Media>>([]);
    const [formValues, setFormValues] = useState({
        user: "",
        pass: "",
        location: localStorage.getItem(localStorageDefaultLocation) || "Zentralbibliothek",
        mediatype: "all",
    });

    const handleUserChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormValues({...formValues, user: e.target.value});
    }
    const handlePassChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormValues({...formValues, pass: e.target.value});
    }
    const handleLocationChange = (location: string) => {
        localStorage.setItem(localStorageDefaultLocation, location)
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
            const message = (e as Error)?.message
            console.log("Error in App: ", message);
            setLoading({isLoading: false, errorMessage: message || "Fehler beim Abrufen"});
        }
    };

    return (
        <div>
            <h1>Merkliste 2.0</h1>

            <form autoComplete="off" onSubmit={handleSubmit}>
                <Box display="flex" flexDirection="column" mb={2}>
                    <TextField required label="Nummer der Kundenkarte"
                               value={formValues.user} onChange={handleUserChange}/>
                    <TextField required label="Passwort" type="password"
                               value={formValues.pass} onChange={handlePassChange}/>
                    <LocationSelect className={classes.form}
                                    location={formValues.location} onSelect={handleLocationChange}/>
                    <MediatypeSelect className={classes.form}
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
