require 'httparty'
require 'nokogiri'

class NED
  include HTTParty
  base_uri 'ned.ipac.caltech.edu'

  def self.by_ra_dec ra, dec, radius
    response = get '/cgi-bin/objsearch', query: { in_csys: 'Equatorial',
                                                  in_equinox: 'J2000',
                                                  lon: "#{ra}d",
                                                  lat: "#{dec}d",
                                                  radius: radius,
                                                  hconst: 73,
                                                  omegam: 0.27,
                                                  omegav: 0.73,
                                                  corr_z: 1,
                                                  z_constraint: 'Unconstrained',
                                                  z_value1: '',
                                                  z_value2: '',
                                                  z_unit: 'z',
                                                  ot_include: 'ANY',
                                                  in_objtypes1: 'Galaxies',
                                                  nmp_op: 'ANY',
                                                  out_csys: 'Equatorial',
                                                  out_equinox: 'J2000',
                                                  obj_sort: 'Distance to search center',
                                                  of: 'xml_basic',
                                                  zv_breaker: 30000.0,
                                                  list_limit: 5,
                                                  img_stamp: 'YES',
                                                  search_type: 'Near Position Search' }

    begin
      format response.parsed_response
    rescue Exception => e
      "Error: #{e.message}"
    end
  end

  def self.format data
    tables = data['VOTABLE']['RESOURCE']['TABLE']
    tables.map do |table|
      fields = table['FIELD'].map { |field| field['name'] }
      data = table['DATA']['TABLEDATA']['TR']['TD']
      hash = Hash[fields.zip data]
      hash['uid'] = hash['refcode']
    end
  end
end