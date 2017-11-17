const functions = require('firebase-functions')
const admin = require('firebase-admin')
const gcs = require('@google-cloud/storage')()
const spawn = require('child-process-promise').spawn

admin.initializeApp(functions.config().firebase)

const ref = admin.database().ref()

exports.test = functions.https.onRequest((req, res) => {
	ref.child('Device').orderByChild('brandName').once('value')
	.then(snap => {
		const  brands = []
		snap.forEach(childSnap => {
			const brand = childSnap.val().brandName
			brands.push(brand)
		})
		res.send('Antwort: ' + brands)
	}).catch(error => {
		res.send(error)
	})
})

exports.generateThimbnail = functions.storage.object()
	.onChange(event => {
		const object = event.data
		const filePath = object.name
		const fileName = filePath.split('/').pop()
		const fileBucket = object.bucket
		const bucket = gcs.bucket(fileBucket)
		const tempFilePath = '/tmp/${fileName}'
		
		if(fileName.startsWith('thumb_')) {
			console.log('Already a Thumbnail.')
			return
		}
		
		if (!object.contentType.startsWith('image/')) {
			console.log('This is not an image')
			return
		}
		
		if (object.resourceState === 'not_exists') {
			console.log('This is a deletion event')
			return
		}
		
		return bucket.file(filePath).download({
			destination: tempFilePath
		})
		.then(() => {
			console.log('Image downloaded locally to', tempFilePath)
			spawn('convert', [tempFilePath, '-thumbnail', '200x200',
				tempFilePath])
		})
		.then(() => {
			console.log('Thumbnail created')
			const thumbFilePath = filePath.replace(/(\/)?([^\/]*)$/,
				'$1thumb_$2')
			
			return bucket.upload(tempFilePath, {
				destination: thumbFilePath
			})
		})
	})