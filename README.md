**<h1>NewsApp_Android</h1>**
Reference Solution Video: https://www.youtube.com/watch?v=RRbNQy4iAYk&t=27s

**<h1>Overview</h1>**
This android application serves as the mobile counterpart of the project: https://github.com/Shashank001122/ReactNewsPortal.


**<h1>Technologies Used</h1>**
Node.JS, Android (Java), Volley library, Glide library, XML, JSON, Google Play Services, APIs

**<h1>APIs Used</h1>**
OpenWeather API, Guardian API, Twitter API, Bing AutoSuggest API, Trending API

**<h1>Features</h1>**
The application makes HTTP requests to the Node server which was already created for website using Volley Library.
As soon as the application opens, it asks permission to show temperature and city name for current location.
Users can store favorite articles which are accessible at all times even after the app is closed. This is done using Shared Preferences.
In case the user want to share the article they can do this using Twitter button which calls Twitter API and shares the card details with a custom message, which users can write before sharing.
Queried Aticles are obtained using Bing Auto Suggest API and displayed in a Recycler View.
Interactive Graphs and Charts are implemented using MP Android Chart.
Splash Screen is implemented before the app loads.
Dynamic load symbol covers the screen when data is being fetched.
Autocomplete TextView is implemented to provide suggestions while searching.
