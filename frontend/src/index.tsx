import React from 'react';
import ReactDOM from 'react-dom';
import {createTheme, StyledEngineProvider, ThemeProvider} from '@mui/material/styles';
import '@fontsource/roboto';
import './index.css';
import App from './App';

const theme = createTheme({
    components: {
        MuiSelect: {
            defaultProps: {
                variant: "standard"
            }
        },
        MuiTextField: {
            defaultProps: {
                variant: "standard"
            }
        },
    },
});

ReactDOM.render(
    <React.StrictMode>
        <StyledEngineProvider injectFirst>
            <ThemeProvider theme={theme}>
                <App/>
            </ThemeProvider>
        </StyledEngineProvider>
    </React.StrictMode>,
    document.getElementById('root')
);
