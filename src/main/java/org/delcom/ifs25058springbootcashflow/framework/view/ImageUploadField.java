package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25058springbootcashflow.adapter.storage.FileStorage;

/** Field upload gambar tunggal (opsional). Data ditahan di memori sampai form disimpan. */
public class ImageUploadField extends Div {

    private final Upload upload;
    private String fileName;
    private byte[] data;

    public ImageUploadField() {
        upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            this.fileName = metadata.fileName();
            this.data = bytes;
        }));
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.addFileRemovedListener(e -> clear());
        add(upload);
    }

    public boolean hasFile() {
        return data != null && data.length > 0;
    }

    public String fileName() {
        return fileName;
    }

    public byte[] data() {
        return data;
    }

    public void clear() {
        fileName = null;
        data = null;
        upload.clearFileList();
    }
}
