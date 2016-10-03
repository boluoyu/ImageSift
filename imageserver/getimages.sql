SELECT DISTINCT location,bin,memex_oculus.imagehash_details.hash FROM memex_oculus.imagehash_details
inner join memex_oculus.images_hash on memex_oculus.imagehash_details.hash=memex_oculus.images_hash.hash 
inner join memex_ht.images on images_id = memex_ht.images.id
where bin > 1000 && bin < 2000