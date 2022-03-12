import React from "react";
import {FormControl, InputLabel, MenuItem, Select} from "@mui/material";

const locations: Array<string> = ["Zentralbibliothek", "Alstertal", "Altona", "Barmbek", "Bergedorf", "Billstedt", "Bramfeld", "Dehnhaide", "Eidelstedt", "Eimsbüttel", "Elbvororte", "Farmsen", "Finkenwerder", "Fuhlsbüttel", "Harburg", "Hohenhorst", "Holstenstraße", "Horn", "Kirchdorf", "Langenhorn", "Lokstedt", "Mümmelmannsberg", "Neuallermöhe", "Neugraben", "Niendorf", "Osdorfer Born", "Rahlstedt", "Schnelsen", "Steilshoop", "Volksdorf", "Wandsbek", "Wilhelmsburg", "Winterhude"];

export const LocationSelect = (props: { location: string, onSelect: (value: string) => void, className?: string }) => {

    return (
        <FormControl className={props.className}>
            <InputLabel>Standort:</InputLabel>
            <Select
                value={props.location}
                onChange={(event) => props.onSelect(event.target.value as string)}
            >
                {locations.map(loc => (
                    <MenuItem value={loc}>{loc}</MenuItem>
                ))}
            </Select>
        </FormControl>
    );
}
