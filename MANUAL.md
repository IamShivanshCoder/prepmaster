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

*Thank you for utilizing PrepMaster! Keep up the spectacular preparation streak.*
