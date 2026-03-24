const puppeteer = require('puppeteer-core');
const path = require('path');
const fs = require('fs');

const name = process.argv[2] || "YOUR NAME HERE";
const regNum = process.argv[3] || "YOUR REG NUMBER HERE";

(async () => {
    try {
        let options = {
            headless: 'new',
            args: ['--no-sandbox', '--disable-setuid-sandbox']
        };

        // Paths for Chrome or Edge in Windows
        const paths = [
            'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
            'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
            'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe'
        ];
        
        for (let p of paths) {
            if (fs.existsSync(p)) {
                options.executablePath = p;
                break;
            }
        }
        
        if (!options.executablePath) {
            console.log("Using downloaded puppeteer chromium...");
            // fallback to full puppeteer if puppeteer-core executable not found
            const fullPuppeteer = require('puppeteer');
            options.executablePath = fullPuppeteer.executablePath();
        }

        const browser = await puppeteer.launch(options);
        const page = await browser.newPage();
        
        const htmlFile = process.argv[4] || 'index.html';
        const fileUrl = 'file:///' + path.join(__dirname, htmlFile).replace(/\\/g, '/');
        console.log("Loading HTML:", fileUrl);
        
        try {
            await page.goto(fileUrl, { waitUntil: 'networkidle0', timeout: 30000 });
        } catch (err) {
            console.log("Navigation timeout, proceeding anyway...");
        }
        
        await new Promise(r => setTimeout(r, 2000));
        
        const outputSuffix = htmlFile === 'index.html' ? 'MediLink' : 'SmartCampus';
        const pdfPath = path.join(__dirname, `${outputSuffix}_Design_Documentation.pdf`);
        
        await page.pdf({
            path: pdfPath,
            format: 'A3',
            printBackground: true,
            displayHeaderFooter: true,
            headerTemplate: '<span></span>',
            footerTemplate: `
              <div style="font-size: 11px; font-family: Arial, sans-serif; width: 100%; text-align: center; border-top: 1px solid #777; padding-top: 5px; margin: 0 15mm; font-weight: bold; color: #444;">
                Name: ${name} &nbsp;&nbsp;|&nbsp;&nbsp; Reg Number: ${regNum} &nbsp;&nbsp;|&nbsp;&nbsp; Project: MediLink App Design
              </div>`,
            margin: { top: '15mm', bottom: '20mm', left: '15mm', right: '15mm' }
        });
        
        await browser.close();
        console.log('PDF Generated Successfully at:', pdfPath);
    } catch (e) {
        console.error("Error generating PDF:", e);
    }
})();
