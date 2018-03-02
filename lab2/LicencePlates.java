class LicencePlates {
        public String Owner;
        public String PlateNumber;

        public LicencePlates(String Owner, String PlateNumber){
            this.Owner = Owner;
            this.PlateNumber = PlateNumber;
        }

        public String getOwner(){
            return Owner;
        }

        public String getPlateNumber(){
            return PlateNumber;
        }
    }