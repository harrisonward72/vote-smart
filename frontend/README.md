# VoteSmartFrontEnd

The purpose of this application arrived from a personal problem I experienced during the 2025 Virginia Governors race, that continued into the 2026 midterm elections. Local, State, Federal and special elections happen all the time. Personally, other than the midterms and presidentials elections I never know what's coming up next. Additionally, it always takes more effort than I wish to put forth to look up who's going to be on my ballet, and then to do the research to really find out about each candidate so that I can make an informed decision. Also, unless I've lived somewhere for several years I need to do further research to find out my polling location and early voting locations. I'm never able to find all the information in one place, and, I find it hard to be able to disseminate factual information from biased information when I'm researching my candidates. AI is nice, but it still doesn't give me the full picture of what I'm looking for.

I believe most people want whats best for this Country, I believe most people believe in fundamental human rights, and, that if you work hard you should be entitled to a comfortable middle class life. I believe that's what most people would vote for given that opportunity. Unfortunately politics brings out the tribal like thinking that is innate to human psychology. We start to look at things like us versus them, red versus blue, etc. Every news source has some sort of bias and it can be overwhelming to try and do proper research to make a truly informed vote. That, I believe, is why so many people just end up voting with their party most of the time. It's how their parents voted, its how they vote.

Vote-Smart provides people with an easy oppotunity to do their own research, to gather non-biased, factual information about things that matter. Things that would normally take some digging to look up, and allows the user to make a side by side comparrison with other candidates running for the same seat (that feature has not been built yet but would be on the final version). It also informs voters on their polling location, when their next upcoming election is and early voting locations.

I envision a finalized version that is able to give more in depth candidate information that goes from the President of the United States all the way down to local government officials. This application is undergoing ongoing development, this is not the final version but a basic start. Some API's that offer deeper information about public officials costs money, and to keep costs low during development they have been omitted. These paid API's would be included in the fully envisioned version of this application.

Also please note some of the data, such as the next upcoming election data, is dummy data. This is because as stated above the API that offers this information is paid. The dummy data has been put in to show the envisioned funcitonality of the application. Other data is real pulled from real API's. Like elected officials based on your entered address and the information about those officials.

If I ever finish development, and figure out how to offset the costs of the paid API's barring sponsors in app adds, or making the platform anything but free I would host it, as I do believe that this concept could be truly useful. However, I do not want adds or sponsors because I think they will hurt the inbiased image I am looking to build, and could try and impact the content that is put forth about certain candidates. I also believe that this application should be free, if users have to pay for it, then they would probably just take the time to do their own research online.

## Running the application

To run the application enter npx ng serve in the terminal for the front end - the backend was developed using the Spring Boot framework with Java and Maven, and is run in IntelliJIDEA. This is how I run the backend. Both the backend and front end must be running to appreciate the full functionality of this application.

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.0.5.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
