# PrepMaster — Administrator & Management Manual

Welcome to **PrepMaster**, your high-fidelity, secure exam preparation application. This guide is designed exclusively for **non-technical administrators and educators** to help you manage the student database, upload study PDFs, and authorize who can access the app—completely for **free** and without writing a single line of code.

---

## Table of Contents
1. [Core Architecture Overview](#1-core-architecture-overview)
2. [How to Manage Allowed Gmail Logins (Whitelist)](#2-how-to-manage-allowed-gmail-logins-whitelist)
3. [How to Organize and Upload Study PDFs](#3-how-to-organize-and-upload-study-pdfs)
4. [Setting Up Your Free Online Database (GitHub Gist)](#4-setting-up-your-free-online-database-github-gist)
5. [Hosting PDFs on Google Drive for Free](#5-hosting-pdfs-on-google-drive-for-free)
6. [Updating the App (Synchronizing)](#6-updating-the-app-synchronizing)
7. [Firebase Authentication (Optional)](#7-firebase-authentication-optional)

---

## 1. Core Architecture Overview

PrepMaster uses a **hybrid synchronization engine** designed to hide all technical operations from your students:
- **Local Cache (Room Database)**: The application stores notes, books, daily streaks, bookmarks, and user session keys locally on the student's device. The app works **100% offline**, allowing students to read condensed "Syllabus Cards" even in areas with no internet.
- **Dynamic Synchronizer (Remote Index)**: When students are online, the app checks a single raw configuration link (configured by you) pointing to a text file (JSON format) containing your PDF document list and authorized emails list.

---

## 2. How to Manage Allowed Gmail Logins (Whitelist)

To ensure **only allowed Google accounts can log into the app**, PrepMaster checks the student's email against your whitelist file.

In your administration file, you will maintain a list under the tag `"whitelist"`. Whitelisted users have instant, secure authorization access.
If an unauthorized student tries to sign in, the app blocks them and shows an alert.

**Example Whitelist:**
```json
"whitelist": [
  "spam.iamshivanshcoder@gmail.com",
  "exammanager@gmail.com",
  "student1@school.edu",
  "student2@gmail.com"
]
```

To remove student access, simply delete their email from the file and run a sync trigger on the app settings page.

---

## 3. How to Organize and Upload Study PDFs

You don't need expensive web hosting or databases to keep your PDFs online. You can host them on **GitHub, Google Drive, OneDrive, or Gist** and catalog them inside your database.

### User Password Management

The `"users"` section stores email-password pairs for centralized login. Passwords are **SHA-256 hashed** (never stored in plaintext).

To generate a SHA-256 hash for a password, use any online SHA-256 generator or run:
```
echo -n "yourpassword" | sha256sum
```

**Default passwords for sample accounts:**
| Email | Password |
|-------|----------|
| `spam.iamshivanshcoder@gmail.com` | `admin` |
| `exammanager@gmail.com` | `manager` |
| `student@school.edu` | `student` |
| `testuser@gmail.com` | `test123` |

> **Note:** If a user exists in `"users"`, the password is verified against the synced hash. If not found in `"users"` but present in `"whitelist"`, the app falls back to per-device password (first login sets the password locally).

Each PDF entry is defined by these simple keys:
- **`id`**: A short, unique identifier for the document (e.g. `math_2025_p1`).
- **`title`**: The name shown to students (e.g. `2025 Mathematics Board Prep Paper`).
- **`subject`**: The folder category to group under (`Mathematics`, `Physics`, `Chemistry`, or `Computer Science`).
- **`category`**: The tab under which the paper belongs (`Past Papers`, `Notes`, `Formula Sheets`, or `Syllabus`).
- **`year`**: The examination year (e.g. `2025` or `N/A`).
- **`url`**: The direct web link where the PDF file is located.
- **`size`**: The file footprint (e.g., `1.4 MB`).

---

## 4. Setting Up Your Free Online Database (GitHub Gist)

Let's set up a completely free, live online index that updates in **1-click**:

1. Go to **[GitHub Gist](https://gist.github.com/)** (create a free GitHub account if you don't have one).
2. Create a new gist file named `prep_database.json`.
3. Copy and paste the template below into the gist:

```json
{
  "whitelist": [
    "spam.iamshivanshcoder@gmail.com",
    "student1@gmail.com",
    "testcandidate@gmail.com"
  ],
  "users": {
    "spam.iamshivanshcoder@gmail.com": "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
    "student1@gmail.com": "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3",
    "testcandidate@gmail.com": "ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae"
  },
  "pdfs": [
    {
      "id": "chem_reaction_2025",
      "title": "2025 Organic Chemistry Core Reactions Sheet",
      "subject": "Chemistry",
      "category": "Formula Sheets",
      "year": "2025",
      "url": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
      "size": "1.2 MB"
    },
    {
      "id": "math_mock_p2",
      "title": "2024 National Mathematics Advanced Prep Paper 2",
      "subject": "Mathematics",
      "category": "Past Papers",
      "year": "2024",
      "url": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
      "size": "3.5 MB"
    }
  ]
}
```

4. Click **Create secret gist** or **Create public gist**.
5. Once saved, look at the top right of your file contents and click the **Raw** button.
6. Copy the browser URL. It will look like this:
   `https://gist.githubusercontent.com/username/gistid/raw/prep_database.json`
7. Paste this URL into the **App Settings (Admin Screen)** and click **Verify & Dynamic Cache Sync**!

The app will download and process all files, configure student login lists, and reload everything instantly.

---

## 5. Hosting PDFs on Google Drive for Free

To host study guide papers on your private Google Drive and make them visible inside the interactive PrepMaster web-viewer:

1. Upload your prep PDF to Google Drive.
2. Right-click the file and choose **Share** -> **Anyone with link can view**.
3. Copy the sharing link. It will look like this:
   `https://drive.google.com/file/d/1A2B3C4D5E6F7G_8H9I_0JkLmNoP/view?usp=sharing`
4. Extract the unique **ID code** of your link. In this example, the ID is:
   `1A2B3C4D5E6F7G_8H9I_0JkLmNoP`
5. Convert this view link into a **Direct Download Link** using this exact format:
   `https://docs.google.com/uc?export=download&id=YOUR_FILE_ID`
   
   In our example, the final direct URL to put in your database JSON is:
   `https://docs.google.com/uc?export=download&id=1A2B3C4D5E6F7G_8H9I_0JkLmNoP`

---

## 6. Updating the App (Synchronizing)

Once your custom JSON database is configured online:
1. Open the PrepMaster application.
2. Authenticate with a whitelisted Google Account (e.g. `spam.iamshivanshcoder@gmail.com`).
3. Tap on the **Admin** tab at the bottom right.
4. Input your Raw Gist/GitHub JSON link in the input box.
5. Tap **Verify & Dynamic Cache Sync**.
6. The sync state dialog will verify the server file, download all materials, and securely update student permissions on-the-fly!

---

---

## 7. Firebase Authentication (Optional)

For production deployments, PrepMaster supports **Firebase Authentication** for secure, centralized password management — no more manual SHA-256 hashing.

### How it works
1. On login, the app calls Firebase's secure REST API to verify the email + password
2. If Firebase is unavailable or not configured, the app **automatically falls back** to local JSON-based password verification
3. This means existing users are never locked out

### Setup Steps

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click **Create a project** (or use an existing one)

2. **Enable Email/Password Authentication**
   - In your Firebase project, go to **Authentication** → **Sign-in method**
   - Click **Email/Password** and enable it
   - Click **Save**

3. **Add users manually** (or let them sign up via Firebase console)
   - In **Authentication** → **Users** tab
   - Click **Add user** and enter their email + password
   - These credentials will be used to log into the app

4. **Get your Web API Key**
   - Go to **Project Settings** → **General**
   - Under **Your apps**, copy the **Web API Key** (e.g. `AIzaSyA1B2C3D4E5F6G7H8I9J0K`)

5. **Configure the app**
   - Open the `.env` file in the project folder
   - Set: `FIREBASE_API_KEY=AIzaSyA1B2C3D4E5F6G7H8I9J0K`
   - (Replace with your actual API key from step 4)

### Security Benefits over JSON approach
- **Hashed passwords server-side** — Firebase never transmits plaintext passwords
- **Rate limiting** — Firebase blocks brute force attempts automatically
- **Account recovery** — Built-in password reset via email
- **No shared secrets** — API key can be restricted to your app's bundle ID

### Important Notes
- Firebase Auth requires internet connectivity for login. If offline, the app falls back to the local JSON `users` map (SHA-256 hashed).
- The **whitelist** and **PDF sync** features continue to work regardless of which auth method is used.
- You can **mix** Firebase users (created in Firebase console) with JSON users (in `prep_database.json`) — the app checks both.

---

*Thank you for utilizing PrepMaster! Keep up the spectacular preparation streak.*
