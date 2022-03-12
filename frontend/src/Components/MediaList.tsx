import React, {useState} from "react";
import {Media} from "../Models/Media";
import {Checkbox, FormControlLabel} from "@mui/material";

const getAvailabilityDisplay = (availability: number): string => {
  switch (availability) {
    case -3:
      return "Abrufen fehlgeschlagen";
    case -2:
      return "Keine Verfügbarkeitsinfos";
    case -1:
      return "Nicht am Standort";
    case 0:
      return "Aktuell nicht verfügbar";
    default:
      return `${availability} verfügbar`;
  }
}

const baseUrl = "https://www.buecherhallen.de/";

export const MediaList = (props: { media: Array<Media>, isLoading: boolean }) => {
  const [showUnavailable, setShowUnavailable] = useState(false);
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setShowUnavailable(event.target.checked);
  }

  if (props.isLoading) {
    return <p>Merkliste wird abgerufen. Dies dauert etwa eine Sekunde pro Merklisteneintrag.</p>;
  } else if (props.media.length === 0) {
    return <p>Merkliste leer oder noch nicht abgerufen.</p>;
  } else {
    return (
        <>
          <p>{props.media.filter(m => m.availability > 0).length} von {props.media.length}</p>
          <FormControlLabel
              control={
                <Checkbox
                    color="primary"
                    checked={showUnavailable}
                    onChange={handleChange}
                />
              }
              label="Nicht verfügbare Medien anzeigen"
          />
          <ol>
            {getMediaList(showUnavailable, props.media).map((item, index) => (
                <li key={index}>
                  <div>
                    <p className={item.availability > 0 ? "available" : "unavailable"}>
                      {item.name} {(item.author || "") === "" ? "" : " - " + item.author}
                    </p>
                    <p>
                      {item.type} - {item.signature}
                    </p>
                    <p>
                      {getAvailabilityDisplay(item.availability)} - {" "}
                      <a href={baseUrl + item.url} target="_blank" rel="noreferrer">
                        Details anzeigen
                        <img src="external-link-ltr-icon.svg" alt="Externer Link öffnet in neuem Unterfenster"/>
                      </a>
                    </p>
                  </div>
                </li>
            ))}
          </ol>
        </>
    );
  }
}

const getMediaList = (showUnavailable: boolean, media: Array<Media>): Array<Media> => showUnavailable
    ? media.sort((first, second) => 0 - (first.name > second.name ? -1 : 1))
    : media.filter(m => m.availability > 0).sort((first, second) => 0 - (first.name > second.name ? -1 : 1))
