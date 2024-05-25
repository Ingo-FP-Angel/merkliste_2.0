import React from "react";
import {FormControl, InputLabel, MenuItem, Select, SxProps} from "@mui/material";

const mediatypes: Map<string, string> = new Map([["Alle", "all"], ["Bücher", "books"], ["Filme/Serien", "movies"], ["Musik", "music"]]);

const generateItems = (entries: Map<string, string>): Array<React.ReactNode> => {
    const items: Array<React.ReactNode> = [];
    entries.forEach((value, key) => {
        items.push(<MenuItem value={value}>{key}</MenuItem>);
    });
    return items;
}

export const MediatypeSelect = (props: { mediatype: string, onSelect: (value: string) => void, sx?: SxProps }) => {

    return (
        <FormControl sx={props.sx}>
            <InputLabel>Medienart:</InputLabel>
            <Select
                value={props.mediatype}
                onChange={(event) => props.onSelect(event.target.value as string)}
            >
                {generateItems(mediatypes)}
            </Select>
        </FormControl>
    );
}
